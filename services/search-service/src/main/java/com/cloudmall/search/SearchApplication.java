package com.cloudmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品搜索服务启动类。
 * <p>
 * 基于 Elasticsearch 提供 C 端商品搜索/浏览列表能力，
 * 通过 product-service 的 search-docs 接口拉取商品文档同步到 ES。
 *
 * @author : Tomatos
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.cloudmall.product.api.client")
public class SearchApplication {

    /**
     * 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
