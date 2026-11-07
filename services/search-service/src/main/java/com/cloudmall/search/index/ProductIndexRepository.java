package com.cloudmall.search.index;

import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.product.api.model.resp.ProductDocResp;
import com.cloudmall.search.properties.SearchProperties;

/**
 * 商品索引写入组件
 * <p>
 * 统一封装 ES 商品索引的写入/删除操作，供 MQ 增量消费与定时增量同步复用。
 * 写入采用字段级 update + docAsUpsert，字段不含 sales，避免覆盖已有销量。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexRepository {

    /** ES 分页拉取文档 ID 的每页条数（search_after 翻页，不受 max_result_window 限制） */
    private static final int FETCH_IDS_PAGE_SIZE = 1_000;

    private final ElasticsearchClient client;
    private final SearchProperties props;

    /**
     * 增量写入单个文档（不存在则新建，字段级更新不覆盖 sales）。
     *
     * @param doc 搜索文档
     */
    public void upsert(ProductDocResp doc) {
        try {
            client.update(
                    u -> u.index(props.getIndex())
                          .id(String.valueOf(doc.getId()))
                          .doc(toIndexFields(doc))
                          .docAsUpsert(true),
                    Object.class
            );
        } catch (IOException e) {
            throw new IllegalStateException("写入 ES 失败 id=" + doc.getId(), e);
        }
    }

    /**
     * 批量写入文档，返回成功条数。
     *
     * @param records 搜索文档列表
     * @return 写入成功条数
     */
    public long upsertBatch(List<ProductDocResp> records) {
        if (CollUtil.isEmpty(records)) {
            return 0;
        }
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (ProductDocResp doc : records) {
            Map<String, Object> fields = toIndexFields(doc);
            br.operations(
                    BulkOperation.of(
                            o -> o.update(
                                    u -> u.index(props.getIndex())
                                          .id(String.valueOf(doc.getId()))
                                          .action(a -> a.doc(fields).docAsUpsert(true))
                            )
                    )
            );
        }
        try {
            BulkResponse response = client.bulk(br.build());
            return response.items()
                           .stream()
                           .filter(item -> item.error() == null)
                           .count();
        } catch (IOException e) {
            throw new IllegalStateException("批量写入 ES 失败", e);
        }
    }

    /**
     * 按商品 ID 删除文档。文档不存在时静默忽略（幂等）。
     *
     * @param id 商品 ID
     */
    public void deleteById(Long id) {
        try {
            client.delete(d -> d.index(props.getIndex()).id(String.valueOf(id)));
        } catch (ElasticsearchException e) {
            // 404：文档不存在，忽略
            if (e.status() != 404) {
                throw e;
            }
            log.debug("ES 文档不存在，忽略删除 id={}", id);
        } catch (IOException e) {
            throw new IllegalStateException("删除 ES 文档失败 id=" + id, e);
        }
    }

    /**
     * 批量删除文档，返回成功条数。
     *
     * @param ids 商品 ID 集合
     * @return 删除成功条数
     */
    public long deleteByIds(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Long id : ids) {
            br.operations(
                    BulkOperation.of(o -> o.delete(d -> d.index(props.getIndex()).id(String.valueOf(id))))
            );
        }
        try {
            BulkResponse response = client.bulk(br.build());
            return response.items()
                           .stream()
                           .filter(item -> item.error() == null)
                           .count();
        } catch (IOException e) {
            throw new IllegalStateException("批量删除 ES 文档失败", e);
        }
    }

    /**
     * 拉取 ES 中全部商品文档 ID。 <p>
     * 按 id 升序 search_after 翻页，不受 max_result_window（默认
     * 10000）限制， 保证全量对账的残留清理覆盖全部文档。
     *
     * @return 文档 ID 集合
     */
    public Set<Long> fetchAllIds() {
        Set<Long> ids = new HashSet<>();
        List<FieldValue> searchAfter = null;
        try {
            while (true) {
                SearchRequest.Builder builder = new SearchRequest.Builder()
                        .index(props.getIndex())
                        .size(FETCH_IDS_PAGE_SIZE)
                        .query(q -> q.matchAll(m -> m))
                        .source(s -> s.filter(f -> f.includes("id")))
                        .sort(so -> so.field(f -> f.field("id").order(SortOrder.Asc)));
                if (searchAfter != null) {
                    builder.searchAfter(searchAfter);
                }
                SearchResponse<JsonData> response = client.search(builder.build(), JsonData.class);
                List<Hit<JsonData>> hits = response.hits().hits();
                if (hits.isEmpty()) {
                    break;
                }
                for (Hit<JsonData> hit : hits) {
                    if (hit.source() == null) {
                        continue;
                    }
                    JsonObject obj = hit.source().to(JsonObject.class);
                    if (obj.containsKey("id")) {
                        ids.add(obj.getJsonNumber("id").longValue());
                    }
                }
                searchAfter = hits.get(hits.size() - 1).sort();
                if (searchAfter == null || searchAfter.isEmpty()) {
                    break;
                }
            }
            return ids;
        } catch (IOException e) {
            throw new IllegalStateException("查询 ES 全部文档 ID 失败", e);
        }
    }

    /**
     * 组装索引字段（不含 sales，避免字段级更新覆盖销量）。
     *
     * @param doc 搜索文档
     * @return 索引字段
     */
    public static Map<String, Object> toIndexFields(ProductDocResp doc) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("id", doc.getId());
        fields.put("name", doc.getName());
        fields.put("spuCode", doc.getSpuCode());
        fields.put("description", doc.getDescription());
        fields.put("categoryId", doc.getCategoryId());
        fields.put("categoryName", doc.getCategoryName());
        fields.put("brandId", doc.getBrandId());
        fields.put("brandName", doc.getBrandName());
        fields.put("shopId", doc.getShopId());
        fields.put("status", doc.getStatus());
        fields.put("priceMin", doc.getPriceMin() == null ? null : doc.getPriceMin().doubleValue());
        fields.put("mainImage", doc.getMainImage());
        if (doc.getCreateTime() != null) {
            fields.put(
                    "createTime",
                    doc.getCreateTime()
                       .atZone(ZoneId.systemDefault())
                       .toInstant()
                       .toEpochMilli()
            );
        }
        if (doc.getUpdateTime() != null) {
            fields.put(
                    "updateTime",
                    doc.getUpdateTime()
                       .atZone(ZoneId.systemDefault())
                       .toInstant()
                       .toEpochMilli()
            );
        }
        return fields;
    }
}
