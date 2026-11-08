package com.cloudmall.mockpayment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.cloudmall.mockpayment.properties.MockPaymentProperties;

/**
 * 模拟支付服务基础配置。
 *
 * @author : Tomatos
 */
@Configuration
@EnableConfigurationProperties(MockPaymentProperties.class)
public class MockPaymentConfig {

    /**
     * 回调通知 HTTP 客户端。
     *
     * @param properties 模拟支付配置
     * @return RestClient
     */
    @Bean
    public RestClient mockPaymentRestClient(MockPaymentProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getNotify().getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getNotify().getReadTimeoutMs());
        return RestClient.builder()
                         .requestFactory(requestFactory)
                         .build();
    }
}
