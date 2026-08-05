package com.cloudmall.search.service.impl;

import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.client.SearchDocClient;
import com.cloudmall.product.api.response.SearchDocPageResp;
import com.cloudmall.product.api.response.SearchDocResp;
import com.cloudmall.search.config.SearchProperties;
import com.cloudmall.search.model.RebuildResultResp;
import com.cloudmall.search.service.ISearchAdminService;

/**
 * 搜索管理服务实现
 * <p>
 * 全量重建：翻页调 product-service search-docs 拉取全量文档，
 * 通过 bulk update（docAsUpsert）写入 ES。写入字段不含 sales，
 * 因此不会覆盖已有销量（销量由独立定时任务维护）。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAdminService implements ISearchAdminService {

    private static final int PAGE_SIZE = 100;

    /** ES 单次查询最大返回条数（默认 max_result_window） */
    private static final int MAX_FETCH_IDS = 10_000;

    private final ElasticsearchClient client;
    private final SearchProperties props;
    private final SearchDocClient searchDocClient;

    /**
     * 全量重建商品索引。
     *
     * @return 重建统计结果
     */
    @Override
    public RebuildResultResp rebuild() {
        long total = 0;
        long updated = 0;
        Set<Long> dbIds = new HashSet<>();

        long page = 1;
        while (true) {
            SearchDocPageResp pageResp = searchDocClient.searchDocs(page, PAGE_SIZE, null);
            List<SearchDocResp> records = pageResp.getRecords();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            total += records.size();
            updated += bulkUpsert(records);
            records.forEach(r -> dbIds.add(r.getId()));
            if (pageResp.getTotal() == null || page * PAGE_SIZE >= pageResp.getTotal()) {
                break;
            }
            page++;
        }

        long deleted = deleteStaleDocs(dbIds);
        log.info("全量重建完成 total={} updated={} deleted={}", total, updated, deleted);
        return RebuildResultResp.builder()
                                .total(total)
                                .updated(updated)
                                .deleted(deleted)
                                .build();
    }

    private long bulkUpsert(List<SearchDocResp> records) {
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (SearchDocResp doc : records) {
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
     * 清理 ES 中 DB 已不存在的残留文档。
     */
    private long deleteStaleDocs(Set<Long> dbIds) {
        Set<Long> esIds = fetchAllIds();
        esIds.removeAll(dbIds);
        if (esIds.isEmpty()) {
            return 0;
        }
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Long id : esIds) {
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
            throw new IllegalStateException("清理 ES 残留文档失败", e);
        }
    }

    private Set<Long> fetchAllIds() {
        try {
            SearchRequest request = SearchRequest.of(
                    r -> r.index(props.getIndex())
                          .size(MAX_FETCH_IDS)
                          .query(q -> q.matchAll(m -> m))
                          .source(s -> s.filter(f -> f.includes("id")))
            );
            SearchResponse<JsonData> response = client.search(request, JsonData.class);
            Set<Long> ids = new HashSet<>();
            for (Hit<JsonData> hit : response.hits().hits()) {
                if (hit.source() == null) {
                    continue;
                }
                JsonObject obj = hit.source().to(JsonObject.class);
                if (obj.containsKey("id")) {
                    ids.add(obj.getJsonNumber("id").longValue());
                }
            }
            return ids;
        } catch (IOException e) {
            throw new IllegalStateException("查询 ES 全部文档 ID 失败", e);
        }
    }

    /**
     * 组装索引字段（不含 sales，避免字段级更新覆盖销量）。
     */
    private static Map<String, Object> toIndexFields(SearchDocResp doc) {
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
