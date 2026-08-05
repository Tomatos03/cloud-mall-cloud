package com.cloudmall.rocketmq.autoconfigure;

import com.cloudmall.rocketmq.producer.MQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * RocketMQ 自动配置
 * <p>
 * 当 classpath 存在 {@link RocketMQTemplate} 且配置启用时，
 * 自动创建 {@link MQProducer} 通用发送客户端 Bean。
 *
 * @author Tomatos
 */
@AutoConfiguration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "cloudmall.rocketmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqAutoConfiguration {

    /**
     * 创建 RocketMQ 通用发送客户端
     *
     * @param rocketMQTemplate RocketMQ 模板（由 rocketmq-spring-boot-starter 自动配置）
     * @return RocketMQProducer 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MQProducer mqProducer(RocketMQTemplate rocketMQTemplate) {
        return new MQProducer(rocketMQTemplate);
    }
}
