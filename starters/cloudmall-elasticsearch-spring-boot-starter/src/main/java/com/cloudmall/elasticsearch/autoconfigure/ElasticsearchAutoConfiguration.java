package com.cloudmall.elasticsearch.autoconfigure;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.cloudmall.elasticsearch.properties.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Elasticsearch 自动配置类
 * <p>
 * 当 classpath 存在 ElasticsearchClient 且配置启用时自动创建 ES 客户端 Bean
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@AutoConfiguration
@ConditionalOnClass(ElasticsearchClient.class)
@ConditionalOnProperty(prefix = "cloudmall.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    /**
     * 创建 ElasticsearchClient Bean
     *
     * @param properties ES 连接配置
     * @return ElasticsearchClient 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchClient elasticsearchClient(ElasticsearchProperties properties) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (properties.getUsername() != null && !properties.getUsername().isEmpty()) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));
        }

        HttpHost[] hosts = parseHosts(properties.getUris());
        RestClientBuilder restClientBuilder = RestClient.builder(hosts)
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    return httpClientBuilder;
                });
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(properties.getConnectTimeout())
                        .setSocketTimeout(properties.getSocketTimeout())
        );

        RestClient restClient = restClientBuilder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    /**
     * 解析 ES 连接地址
     *
     * @param uris 逗号分隔的地址字符串
     * @return HttpHost 数组
     */
    private HttpHost[] parseHosts(String uris) {
        return java.util.Arrays.stream(uris.split(","))
                .map(String::trim)
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
    }
}
