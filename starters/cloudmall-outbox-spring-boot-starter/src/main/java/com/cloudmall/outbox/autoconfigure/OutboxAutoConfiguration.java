package com.cloudmall.outbox.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.cloudmall.outbox.appender.OutboxAppender;
import com.cloudmall.outbox.aspect.OutboxEventAspect;
import com.cloudmall.outbox.mapper.OutboxMapper;
import com.cloudmall.outbox.properties.OutboxProperties;
import com.cloudmall.outbox.repository.OutboxRepository;

/**
 * Outbox 自动配置。
 * <p>
 * 装配写入器、注解切面与通用消费操作；表结构固定为 {@code cm_message_outbox}。
 * <p>
 * 通过 {@link AutoConfigurationPackage} 注册 {@code com.cloudmall.outbox.mapper}，
 * 并以 {@link AutoConfiguration} 的 before 属性保证该包先于 MyBatis 自动扫描器
 * 写入 AutoConfigurationPackages，使 {@link OutboxMapper} 被自动扫描注册，
 * 消费方无需额外 {@code @MapperScan}。
 *
 * @author : Tomatos
 */
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnClass({Aspect.class, MybatisPlusInterceptor.class})
@AutoConfigurationPackage(basePackages = "com.cloudmall.outbox.mapper")
@AutoConfiguration(beforeName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
public class OutboxAutoConfiguration {

    /**
     * 创建事件写入器。
     *
     * @param outboxMapper 通用 Outbox Mapper
     * @param environment 环境（读取 spring.application.name）
     * @return 事件写入器
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxAppender outboxAppender(OutboxMapper outboxMapper, Environment environment) {
        return new OutboxAppender(outboxMapper, environment.getRequiredProperty("spring.application.name"));
    }

    /**
     * 创建注解切面。
     *
     * @param appender 事件写入器
     * @param transactionManager 事务管理器
     * @param beanFactory Bean 工厂（用于 SpEL 引用 Bean）
     * @return 注解切面
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxEventAspect outboxEventAspect(
        OutboxAppender appender,
        PlatformTransactionManager transactionManager,
        BeanFactory beanFactory
    ) {
        return new OutboxEventAspect(appender, new TransactionTemplate(transactionManager), beanFactory);
    }

    /**
     * 创建通用消费操作。
     *
     * @param outboxMapper 通用 Outbox Mapper
     * @param transactionManager 事务管理器
     * @param properties 运行参数
     * @return 通用消费操作
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxRepository outboxRepository(
        OutboxMapper outboxMapper,
        PlatformTransactionManager transactionManager,
        OutboxProperties properties
    ) {
        return new OutboxRepository(outboxMapper, new TransactionTemplate(transactionManager), properties);
    }
}
