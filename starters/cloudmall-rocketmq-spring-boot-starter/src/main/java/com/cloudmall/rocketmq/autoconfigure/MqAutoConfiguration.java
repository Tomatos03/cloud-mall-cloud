package com.cloudmall.rocketmq.autoconfigure;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * RocketMQ 自动配置
 * <p>
 * 当 classpath 存在 {@link RocketMQTemplate} 且配置启用时，
 * 自动创建 {@link MQProducer} 通用发送客户端 Bean。
 * <p>
 * 消费幂等台账（{@code MessageConsumeService}）由
 * {@link MessageConsumeAutoConfiguration} 按 MyBatis-Plus 是否在场
 * 条件装配，本配置类不引用任何 MyBatis-Plus 类型，
 * 仅引入 rocketmq 依赖（无 mp）的项目仍可正常使用发送能力。
 *
 * @author Tomatos
 */
@AutoConfiguration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(
        prefix = "cloudmall.rocketmq",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MqAutoConfiguration {

    /**
     * 创建 RocketMQ 通用发送客户端。
     *
     * @param rocketMQTemplate RocketMQ 模板（由
     * rocketmq-spring-boot-starter
     *                         自动配置）
     * @return MQProducer 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MQProducer mqProducer(RocketMQTemplate rocketMQTemplate) {
        return new MQProducer(rocketMQTemplate);
    }
}
