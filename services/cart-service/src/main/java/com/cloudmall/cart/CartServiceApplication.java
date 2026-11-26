package com.cloudmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 购物车服务启动入口。
 *
 * @author : Tomatos
 */
@EnableDiscoveryClient
@SpringBootApplication
public class CartServiceApplication {

    /**
     * 启动应用程序。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
