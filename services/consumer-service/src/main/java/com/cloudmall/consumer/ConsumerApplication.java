package com.cloudmall.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 消息消费服务启动类
 * <p>
 * 独立部署的 RocketMQ 消费者，消费业务服务通过事务消息发布的事件，
 * 经 Feign 调用对应业务服务完成处理。
 *
 * @author : Tomatos
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ConsumerApplication {

    /**
     * 启动应用程序。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
