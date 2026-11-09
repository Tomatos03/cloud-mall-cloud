package com.cloudmall.search.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.model.resp.ProductDocResp;
import com.cloudmall.search.api.model.req.SearchProductReq;
import com.cloudmall.search.api.model.resp.SearchFacetsResp;
import com.cloudmall.search.api.model.resp.SearchProductResp;
import com.cloudmall.search.api.model.resp.SearchResultResp;
import com.cloudmall.search.index.ProductIndexRepository;
import com.cloudmall.search.model.bo.ProductHit;
import com.cloudmall.search.model.resp.RebuildResultResp;
import com.cloudmall.search.properties.SearchProperties;
import com.cloudmall.search.properties.SearchSyncProperties;
import com.cloudmall.search.service.ISearchService;

/**
 * 商品搜索服务实现
 * <p>
 * 查询 DSL：keyword 多字段匹配（name^3 / description^2 / spuCode / brandName / categoryName），
 * 上架状态 + 分类/品牌/价格/店铺过滤，分类/品牌 terms 聚合与去重数量，name 高亮。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int MAX_FACET_SIZE = 20;

    private final ElasticsearchClient client;
    private final SearchProperties props;
    private final ProductClient productClient;
    private final ProductIndexRepository productIndexRepository;
    private final SearchSyncProperties syncProps;
    /**
     * 搜索商品。
     *
     * @param req 搜索请求
     * @return 搜索分页结果
     */
    @Override
    public SearchResultResp search(SearchProductReq req) {
        int page = req.getPage() == null ? 1 : req.getPage();
        int size = req.getPageSize() == null ? DEFAULT_SIZE : Math.min(req.getPageSize(), MAX_SIZE);
        int from = Math.max((page - 1) * size, 0);

        SearchRequest searchRequest = buildSearchRequest(req, from, size);
        SearchResponse<ProductHit> response = execute(searchRequest);
        return assembleResult(response, page, size);
    }

    /**
     * 清理 ES 中 DB 已不存在的残留文档。
     *
     * @param dbIds DB 中现存商品 ID 集合
     * @return 删除条数
     */
    private long deleteStaleDocs(Set<Long> dbIds) {
        Set<Long> esIds = productIndexRepository.fetchAllIds();
        esIds.removeAll(dbIds);
        return productIndexRepository.deleteByIds(esIds);
    }

    /**
     * 全量重建商品索引。
     *
     * @return 重建统计结果
     */
    @Override
    public RebuildResultResp rebuild() {
        SearchSyncProperties.FullSync full = syncProps.getFull();
        long total = 0;
        long updated = 0;
        long failed = 0;
        Set<Long> dbIds = new HashSet<>();

        long cursor = 0;
        long pages = 0;
        while (true) {
            if (full.getMaxPages() > 0 && pages >= full.getMaxPages()) {
                log.warn("全量重建达到最大翻页数上限 maxPages={}，提前结束", full.getMaxPages());
                break;
            }
            CursorResult<ProductDocResp> result = productClient.cursorPage(
                    CursorReq.builder()
                             .cursor(cursor)
                             .size(full.getPageSize())
                             .build()
            );
            List<ProductDocResp> records = result.getItems();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            total += records.size();
            long batchUpdated = productIndexRepository.upsertBatch(records);
            updated += batchUpdated;
            failed += records.size() - batchUpdated;
            records.forEach(r -> dbIds.add(r.getId()));
            if (!result.getHasMore()) {
                break;
            }
            cursor = result.getCursor();
            pages++;
        }

        long deleted = deleteStaleDocs(dbIds);
        log.info("全量重建完成 total={} updated={} deleted={} failed={}", total, updated, deleted, failed);
        return RebuildResultResp.builder()
                                .total(total)
                                .updated(updated)
                                .deleted(deleted)
                                .failed(failed)
                                .build();
    }

    private SearchRequest buildSearchRequest(SearchProductReq req, int from, int size) {
        String keyword = StrUtil.trimToNull(req.getKeyword());
        SortOptions sort = buildSort(req.getSortBy());

        return SearchRequest.of(
                r -> {
                    r
                    .index(props.getIndex())
                    .from(from)
                    .size(size)
                    .query(
                            q -> q.bool(
                                    b -> {
                                        if (keyword != null) {
                                            b.must(
                                                    m -> m.multiMatch(
                                                            mm -> mm.query(keyword)
                                                                    .fields(
                                                                            List.of(
                                                                                    "name^3",
                                                                                    "description^2",
                                                                                    "spuCode",
                                                                                    "brandName",
                                                                                    "categoryName"
                                                                            )
                                                                    )
                                                    )
                                            );
                                        }
                                        b.filter(buildFilters(req));
                                        return b;
                                    }
                            )
                    )
                    .aggregations(
                            "categories",
                            a -> a.terms(t -> t.field("categoryId").size(MAX_FACET_SIZE))
                                  .aggregations(
                                          "name",
                                          n -> n.topHits(
                                                  th -> th.size(1)
                                                          .source(
                                                                  s -> s.filter(
                                                                          f -> f.includes("categoryName")
                                                                  )
                                                          )
                                          )
                                  )
                    )
                    .aggregations(
                            "brands",
                            a -> a.terms(t -> t.field("brandId").size(MAX_FACET_SIZE))
                                  .aggregations(
                                          "name",
                                          n -> n.topHits(
                                                  th -> th.size(1)
                                                          .source(s -> s.filter(f -> f.includes("brandName")))
                                          )
                                  )
                    )
                    .aggregations("categoryCount", a -> a.cardinality(c -> c.field("categoryId")))
                    .aggregations("brandCount", a -> a.cardinality(c -> c.field("brandId")))
                    .highlight(h -> h.fields("name", f -> f.preTags("<em>").postTags("</em>")));
                    if (sort != null) {
                        r.sort(sort);
                    }
                    return r;
                }
        );
    }

    private List<Query> buildFilters(SearchProductReq req) {
        List<Query> filters = new ArrayList<>();
        filters.add(Query.of(q -> q.term(t -> t.field("status").value(1))));
        if (req.getCategoryId() != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("categoryId").value(req.getCategoryId()))));
        }
        if (req.getBrandId() != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("brandId").value(req.getBrandId()))));
        }
        if (req.getShopId() != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("shopId").value(req.getShopId()))));
        }
        if (req.getPriceMin() != null || req.getPriceMax() != null) {
            NumberRangeQuery.Builder range = new NumberRangeQuery.Builder().field("priceMin");
            if (req.getPriceMin() != null) {
                range.gte(req.getPriceMin());
            }
            if (req.getPriceMax() != null) {
                range.lte(req.getPriceMax());
            }
            filters.add(Query.of(q -> q.range(r -> r.number(range.build()))));
        }
        return filters;
    }

    private SortOptions buildSort(String sortBy) {
        if (StrUtil.isBlank(sortBy) || "relevance".equals(sortBy)) {
            return null;
        }
        return switch (sortBy) {
            case "price_asc" -> SortOptions.of(s -> s.field(f -> f.field("priceMin").order(SortOrder.Asc)));
            case "price_desc" -> SortOptions.of(s -> s.field(f -> f.field("priceMin").order(SortOrder.Desc)));
            case "sales" -> SortOptions.of(s -> s.field(f -> f.field("sales").order(SortOrder.Desc)));
            case "newest" -> SortOptions.of(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc)));
            default -> throw new BizException(BizErrorEnum.UNKNOWN_SEARCH_ORDER_TYPE);
        };
    }

    private SearchResponse<ProductHit> execute(SearchRequest request) {
        try {
            return client.search(request, ProductHit.class);
        } catch (IOException e) {
            log.error("ES 搜索失败 request={}", request, e);
            throw new IllegalStateException("ES 搜索失败", e);
        }
    }

    private SearchResultResp assembleResult(SearchResponse<ProductHit> response, int page, int size) {
        long total = response.hits().total() == null ? 0 : response.hits()
                                                                   .total()
                                                                   .value();
        long pages = (total + size - 1) / size;
        return SearchResultResp.builder()
                               .items(buildItems(response))
                               .facets(buildFacets(response))
                               .total(total)
                               .page((long) page)
                               .pageSize((long) size)
                               .pages(pages)
                               .build();
    }

    private List<SearchProductResp> buildItems(SearchResponse<ProductHit> response) {
        return response.hits()
                       .hits()
                       .stream()
                       .map(this::toSearchProductResp)
                       .filter(Objects::nonNull)
                       .toList();
    }

    private SearchProductResp toSearchProductResp(Hit<ProductHit> hit) {
        ProductHit doc = hit.source();
        if (doc == null) {
            return null;
        }
        String highlightName = extractHighlightName(hit, doc.getName());
        return SearchProductResp.builder()
                                .id(doc.getId())
                                .name(doc.getName())
                                .description(doc.getDescription())
                                .spuCode(doc.getSpuCode())
                                .mainImage(doc.getMainImage())
                                .priceMin(doc.getPriceMin())
                                .sales(doc.getSales())
                                .categoryId(doc.getCategoryId())
                                .categoryName(doc.getCategoryName())
                                .brandId(doc.getBrandId())
                                .brandName(doc.getBrandName())
                                .shopId(doc.getShopId())
                                .highlightName(highlightName)
                                .build();
    }

    private String extractHighlightName(Hit<ProductHit> hit, String fallback) {
        if (hit.highlight() == null) {
            return fallback;
        }
        List<String> fragments = hit.highlight().get("name");
        if (fragments == null || fragments.isEmpty()) {
            return fallback;
        }
        return fragments.stream()
                        .filter(StrUtil::isNotBlank)
                        .reduce((a, b) -> a + "…" + b)
                        .orElse(fallback);
    }

    private SearchFacetsResp buildFacets(SearchResponse<ProductHit> response) {
        if (response.aggregations() == null) {
            return SearchFacetsResp.builder()
                                   .categories(List.of())
                                   .brands(List.of())
                                   .categoryCount(0L)
                                   .brandCount(0L)
                                   .build();
        }
        return SearchFacetsResp
                .builder()
                .categories(
                        FacetAggregationParser.parse(
                                response.aggregations().get("categories"),
                                "categoryName"
                        )
                )
                .brands(FacetAggregationParser.parse(response.aggregations().get("brands"), "brandName"))
                .categoryCount(
                        FacetAggregationParser.cardinality(response.aggregations().get("categoryCount"))
                )
                .brandCount(FacetAggregationParser.cardinality(response.aggregations().get("brandCount")))
                .build();
    }
}
