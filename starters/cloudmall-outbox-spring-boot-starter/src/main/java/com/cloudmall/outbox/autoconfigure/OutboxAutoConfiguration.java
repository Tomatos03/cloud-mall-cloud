package com.cloudmall.outbox.autoconfigure;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.cloudmall.outbox.appender.OutboxAppender;
import com.cloudmall.outbox.config.OutboxProperties;
import com.cloudmall.outbox.dispatcher.OutboxDispatcher;
import com.cloudmall.outbox.job.OutboxJobHandlers;
import com.cloudmall.outbox.mapper.OutboxMapper;
import com.cloudmall.outbox.publisher.OutboxPublisher;
import com.cloudmall.outbox.publisher.RocketMqOutboxPublisher;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * Outbox 自动配置
 * <p>
 * 装配通用追加器、分发器与默认 RocketMQ 发布器。载荷为原始 JSON 字节，
 * 生产者不感知事件结构，无需事件注册表。
 * <p>
 * 发布器面向 {@link OutboxPublisher} 抽象：默认提供 RocketMQ 实现，
 * 业务服务可注册自定义 Bean 替换（如 Feign 发布），分发器无需改动。
 * <p>
 * 通过 {@link AutoConfigurationPackage} 注册 com.cloudmall.outbox，
 * 并以 {@link AutoConfiguration} 的 before 属性保证该包先于 MyBatis 自动扫描器
 * 写入 AutoConfigurationPackages，使 OutboxMapper 被 @Mapper 自动扫描注册，
 * 同时不引入 MapperScannerConfigurer、不影响消费方自身 Mapper 的自动扫描。
 *
 * @author : Tomatos
 */
@EnableConfigurationProperties(OutboxProperties.class)
@AutoConfigurationPackage(basePackages = "com.cloudmall.outbox.mapper")
@AutoConfiguration(
        beforeName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
        afterName = "com.cloudmall.rocketmq.autoconfigure.MqAutoConfiguration"
)
public class OutboxAutoConfiguration {

    /**
     * 创建事件追加器。 <p>
     * 注意：不能在此处使用
     * {@code @ConditionalOnBean(OutboxMapper.class)}—— 条
     * 件在配置类解析时评估，而 OutboxMapper 由 MyBatis 扫描器（BDRPP）在之后才
     * 注册， 条件必然失败。改为直接构造器注入，实例化时扫描已完成。
     *
     * @param outboxMapper 通用 Outbox Mapper
     * @param environment 环境（读取 spring.application.name）
     * @return 事件追加器
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public OutboxAppender outboxAppender(OutboxMapper outboxMapper, Environment environment) {
        return new OutboxAppender(outboxMapper, appName(environment));
    }

    /**
     * 创建默认 RocketMQ 发布器。 <p>
     * 依赖 {@link MQProducer} Bean（由
     * {@code MqAutoConfiguration} 提供）， 业务服务可通过自定义
     * {@link OutboxPublisher} Bean 覆盖。
     *
     * @param mqProducer RocketMQ 发送客户端
     * @return RocketMQ 发布器
     */
    @Bean
    @ConditionalOnBean(MQProducer.class)
    @ConditionalOnClass(MQProducer.class)
    @ConditionalOnMissingBean(OutboxPublisher.class)
    public RocketMqOutboxPublisher rocketMqOutboxPublisher(MQProducer mqProducer) {
        return new RocketMqOutboxPublisher(mqProducer);
    }

    /**
     * 创建通用分发器。
     *
     * @param outboxMapper 通用 Outbox Mapper
     * @param publisher 事件发布器（默认为 RocketMQ 实现）
     * @param properties Outbox 配置
     * @param environment 环境（读取 spring.application.name）
     * @return 通用分发器
     */
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public OutboxDispatcher outboxDispatcher(
        OutboxMapper outboxMapper,
        OutboxPublisher publisher,
        OutboxProperties properties,
        Environment environment
    ) {
        return new OutboxDispatcher(outboxMapper, publisher, appName(environment), properties);
    }

    /**
     * 创建默认调度任务。 <p>
     * classpath 存在 xxl-job 且分发器已装配时才生效；服务可声明同名 Bean 覆盖默认
     * 实现。
     *
     * @param outboxDispatcher 通用分发器
     * @param properties 配置属性
     * @return 默认调度任务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(XxlJob.class)
    @ConditionalOnBean(OutboxDispatcher.class)
    public OutboxJobHandlers outboxJobHandlers(OutboxDispatcher outboxDispatcher, OutboxProperties properties) {
        return new OutboxJobHandlers(outboxDispatcher, properties);
    }

    /**
     * 解析来源服务名，缺失时快速失败。
     *
     * @param environment 环境
     * @return spring.application.name
     */
    private static String appName(Environment environment) {
        return environment.getRequiredProperty("spring.application.name");
    }
}
