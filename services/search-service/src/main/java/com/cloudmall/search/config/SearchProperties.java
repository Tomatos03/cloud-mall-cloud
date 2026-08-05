package com.cloudmall.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 搜索服务配置属性
 * <p>
 * 对应 Nacos 配置 search-service.yaml 的 {@code search.elasticsearch.*} 段。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "search.elasticsearch")
public class SearchProperties {

    /** Elasticsearch 地址，如 http://localhost:9200 */
    private String uris = "http://localhost:9200";

    /** 商品搜索索引名 */
    private String index = "cm_product";
}
