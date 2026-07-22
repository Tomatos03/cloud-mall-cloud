package com.cloudmall.elasticsearch.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Elasticsearch 连接配置属性
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "cloudmall.elasticsearch")
public class ElasticsearchProperties {

    /** 是否启用 Elasticsearch */
    private boolean enabled = true;

    /** ES 连接地址，多个用逗号分隔 */
    private String uris = "http://localhost:9200";

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 连接超时(毫秒) */
    private int connectTimeout = 5000;

    /** Socket 超时(毫秒) */
    private int socketTimeout = 60000;
}
