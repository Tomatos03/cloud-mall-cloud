package com.cloudmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 商品搜索服务启动类。
 * <p>
 * 基于 Elasticsearch 提供 C 端商品搜索/浏览列表能力，
 * 通过 product-service 的 docs 接口拉取商品文档同步到 ES。
 * <p>
 * Feign 客户端由 cloudmall-feign-spring-boot-starter 统一扫描注册，
 * 此处不再重复声明 {@code @EnableFeignClients}，避免同一客户端被注册两次。
 *
 * @author : Tomatos
 */
@EnableScheduling
@SpringBootApplication
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
