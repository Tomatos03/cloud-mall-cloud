package com.cloudmall.search.service.impl;

import java.util.List;
import java.util.Map;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import jakarta.json.JsonObject;

import com.cloudmall.search.api.model.resp.FacetItemResp;

/**
 * 搜索聚合结果解析工具。
 * <p>
 * ES terms 聚合会按字段类型返回 {@code lterms}（long）或 {@code sterms}（string），
 * 分类/品牌 ID 均为 long，因此解析时需要同时兼容两种桶类型。
 *
 * @author : Tomatos
 */
final class FacetAggregationParser {

    private FacetAggregationParser() {}

    /**
     * 解析 terms 聚合为筛选项列表。
     *
     * @param aggregate 聚合节点
     * @param nameField 桶内 top_hits 中读取名称的字段
     * @return 筛选项列表
     */
    static List<FacetItemResp> parse(Aggregate aggregate, String nameField) {
        if (aggregate == null) {
            return List.of();
        }
        if (aggregate.isLterms()) {
            return aggregate
                    .lterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(
                            bucket -> toFacetItem(
                                    bucket.key(),
                                    bucket.docCount(),
                                    bucket.aggregations(),
                                    nameField
                            )
                    )
                    .toList();
        }
        if (aggregate.isSterms()) {
            return aggregate
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(
                            bucket -> toFacetItem(
                                    bucket.key(),
                                    bucket.docCount(),
                                    bucket.aggregations(),
                                    nameField
                            )
                    )
                    .toList();
        }
        return List.of();
    }

    /**
     * 读取 cardinality 聚合结果。
     *
     * @param aggregate 聚合节点
     * @return 去重数量，缺失时为 0
     */
    static long cardinality(Aggregate aggregate) {
        if (aggregate == null || !aggregate.isCardinality()) {
            return 0L;
        }
        return aggregate.cardinality().value();
    }

    /**
     * 将 long terms 桶转换为筛选项。
     *
     * @param key 聚合键
     * @param docCount 命中数量
     * @param aggregations 桶内子聚合
     * @param nameField 名称字段
     * @return 筛选项
     */
    private static FacetItemResp toFacetItem(
        long key,
        long docCount,
        Map<String, Aggregate> aggregations,
        String nameField
    ) {
        return FacetItemResp.builder()
                            .id(key)
                            .name(resolveName(aggregations, nameField))
                            .count(docCount)
                            .build();
    }

    /**
     * 将 string terms 桶转换为筛选项。
     *
     * @param key 聚合键
     * @param docCount 命中数量
     * @param aggregations 桶内子聚合
     * @param nameField 名称字段
     * @return 筛选项
     */
    private static FacetItemResp toFacetItem(
        FieldValue key,
        long docCount,
        Map<String, Aggregate> aggregations,
        String nameField
    ) {
        return FacetItemResp.builder()
                            .id(parseId(key))
                            .name(resolveName(aggregations, nameField))
                            .count(docCount)
                            .build();
    }

    /**
     * 解析 string terms 桶键为数字 ID。
     *
     * @param key 聚合键
     * @return 数字 ID，无法解析时为 null
     */
    private static Long parseId(FieldValue key) {
        try {
            return Long.valueOf(key.stringValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从桶内 top_hits 子聚合中读取名称。
     *
     * @param aggregations 桶内子聚合
     * @param nameField 名称字段
     * @return 名称，缺失时为 null
     */
    private static String resolveName(Map<String, Aggregate> aggregations, String nameField) {
        Aggregate nameAgg = aggregations == null ? null : aggregations.get("name");
        if (nameAgg == null || !nameAgg.isTopHits()) {
            return null;
        }
        TopHitsAggregate topHits = nameAgg.topHits();
        HitsMetadata<JsonData> hitsMetadata = topHits.hits();
        if (hitsMetadata == null) {
            return null;
        }
        List<Hit<JsonData>> hits = hitsMetadata.hits();
        if (hits == null || hits.isEmpty()) {
            return null;
        }
        JsonData source = hits.get(0).source();
        if (source == null) {
            return null;
        }
        if (!(source.toJson() instanceof JsonObject obj)) {
            return null;
        }
        return obj.containsKey(nameField) ? obj.getString(nameField) : null;
    }
}
