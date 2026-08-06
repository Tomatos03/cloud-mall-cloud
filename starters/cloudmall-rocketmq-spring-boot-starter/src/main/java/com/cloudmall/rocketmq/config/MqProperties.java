package com.cloudmall.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ 自动配置属性
 * <p>
 * 前缀 cloudmall.rocketmq。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "cloudmall.rocketmq")
public class MqProperties {

    /** 消费幂等消费者标识，默认取 spring.application.name */
    private String consumer = "";
}
