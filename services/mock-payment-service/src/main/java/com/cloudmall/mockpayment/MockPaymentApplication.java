package com.cloudmall.mockpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 模拟支付服务启动类。
 * <p>
 * 以微信支付 API v3 为蓝本，提供下单、查询、关单与异步回调能力，
 * 供 Cloud Mall 在本地开发与联调阶段替代真实第三方支付渠道。
 *
 * @author : Tomatos
 */
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class MockPaymentApplication {

    /**
     * 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MockPaymentApplication.class, args);
    }
}
