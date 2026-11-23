package com.cloudmall.shared;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 公共服务启动类。
 * <p>
 * 统一承载文件上传、访问、台账与过期回收等跨服务通用能力。
 *
 * @author : Tomatos
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SharedServiceApplication {

    /**
     * 启动应用程序。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SharedServiceApplication.class, args);
    }
}
