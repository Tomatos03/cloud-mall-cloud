package com.cloudmall.aigc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class AIGCApplication {

    /**
     * 启动应用程序。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AIGCApplication.class, args);
    }
}
