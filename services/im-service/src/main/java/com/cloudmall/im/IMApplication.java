package com.cloudmall.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * IM 服务启动类
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@EnableDiscoveryClient
@SpringBootApplication
public class IMApplication {
    public static void main(String[] args) {
        SpringApplication.run(IMApplication.class, args);
    }
}
