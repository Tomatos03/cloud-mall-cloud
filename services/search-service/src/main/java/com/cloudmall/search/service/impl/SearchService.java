package com.cloudmall.search.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.search.api.request.SearchProductReq;
import com.cloudmall.search.api.response.FacetItemResp;
import com.cloudmall.search.api.response.SearchFacetsResp;
import com.cloudmall.search.api.response.SearchProductResp;
import com.cloudmall.search.api.response.SearchResultResp;
import com.cloudmall.search.config.SearchProperties;
import com.cloudmall.search.model.ProductHit;
import com.cloudmall.search.service.ISearchService;

/**
 * 商品搜索服务实现
 * <p>
 * 查询 DSL：keyword 多字段匹配（name^3 / description^2 / spuCode / brandName / categoryName），
 * 上架状态 + 分类/品牌/价格/店铺过滤，分类/品牌 terms 聚合，name 高亮。
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
            default -> throw new BizException(BizError.UNKNOWN_SEARCH_ORDER_TYPE);
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
                                   .build();
        }
        return SearchFacetsResp
                .builder()
                .categories(buildFacetItems(response.aggregations().get("categories"), "categoryName"))
                .brands(buildFacetItems(response.aggregations().get("brands"), "brandName"))
                .build();
    }

    private List<FacetItemResp> buildFacetItems(Aggregate aggregate, String nameField) {
        if (aggregate == null || !aggregate.isSterms() || aggregate.sterms().buckets() == null) {
            return List.of();
        }
        return aggregate
                .sterms()
                .buckets()
                .array()
                .stream()
                .map(
                        bucket -> FacetItemResp.builder()
                                               .id(parseBucketId(bucket))
                                               .name(resolveBucketName(bucket, nameField))
                                               .count(bucket.docCount())
                                               .build()
                )
                .toList();
    }

    private static Long parseBucketId(StringTermsBucket bucket) {
        try {
            return Long.valueOf(bucket.key().stringValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveBucketName(StringTermsBucket bucket, String nameField) {
        Aggregate nameAgg = bucket.aggregations() == null ? null : bucket.aggregations().get("name");
        if (
                nameAgg == null || !nameAgg.isTopHits() || nameAgg.topHits().hits() == null
                    || nameAgg.topHits().hits().hits() == null || nameAgg.topHits().hits().hits().isEmpty()
        ) {
            return null;
        }
        JsonData source = nameAgg.topHits()
                                 .hits()
                                 .hits()
                                 .get(0)
                                 .source();
        if (source == null) {
            return null;
        }
        JsonObject obj = source.to(JsonObject.class);
        return obj.containsKey(nameField) ? obj.getString(nameField) : null;
    }
}
