package com.cloudmall.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置
 * <p>
 * 使用官方 {@code elasticsearch-java} 客户端：
 * RestClient → RestClientTransport → ElasticsearchClient。
 * Jackson 注册 JSR-310 模块（LocalDateTime/Instant），并忽略未知字段。
 *
 * @author : Tomatos
 */
@Configuration
@EnableConfigurationProperties({SearchProperties.class, SearchSyncProperties.class})
public class ElasticsearchConfig {

    /**
     * 构建 ES 客户端。
     *
     * @param props 搜索配置
     * @return ES 客户端
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(SearchProperties props) {
        ObjectMapper mapper = JsonMapper.builder()
                                        .addModule(new JavaTimeModule())
                                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                        .build();
        RestClient restClient = RestClient.builder(HttpHost.create(props.getUris())).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
        return new ElasticsearchClient(transport);
    }
}
