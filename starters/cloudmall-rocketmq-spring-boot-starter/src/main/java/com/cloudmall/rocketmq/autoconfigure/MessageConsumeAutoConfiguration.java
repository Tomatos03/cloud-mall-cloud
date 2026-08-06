package com.cloudmall.rocketmq.autoconfigure;

import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.cloudmall.rocketmq.config.MqProperties;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;
import com.cloudmall.rocketmq.consumer.MessageConsumeService;
import com.cloudmall.rocketmq.mapper.MessageConsumeRecordMapper;

/**
 * 消费幂等台账自动配置
 * <p>
 * 仅在 classpath 存在 MyBatis-Plus（{@code BaseMapper}）时生效：
 * <ul>
 * <li>用 {@link MapperFactoryBean} 显式注册 {@link MessageConsumeRecordMapper}
 * ——不依赖 @Mapper 自动扫描与 AutoConfigurationPackages 的注册顺序，
 * 也不注册 MapperScannerConfigurer，不影响消费方自身 Mapper 的自动扫描；</li>
 * <li>自动装配消费幂等 {@link MessageConsumeService}（实现
 * {@link IMessageConsumeService}），consumer 标识默认取
 * spring.application.name，可用 cloudmall.rocketmq.consumer 覆盖。</li>
 * </ul>
 * 仅引入 rocketmq 依赖（无 MyBatis-Plus）的项目，本配置类整体跳过，不报错。
 *
 * @author : Tomatos
 */
@AutoConfiguration
@EnableConfigurationProperties(MqProperties.class)
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
public class MessageConsumeAutoConfiguration {

    /**
     * 注册消费幂等台账 Mapper。 <p>
     * 通过 {@link MapperFactoryBean} 直接创建 Mapper 代理，Mapper
     * bean 在实例化阶段由 SqlSessionFactory 解析，与 MyBatis 自动扫描完全
     * 解耦。
     *
     * @param sqlSessionFactory MyBatis SqlSessionFactory
     * @return Mapper 工厂 Bean（生产
     *         MessageConsumeRecordMapper 代理）
     */
    @Bean
    public MapperFactoryBean<MessageConsumeRecordMapper> messageConsumeRecordMapper(
            SqlSessionFactory sqlSessionFactory
    ) {
        MapperFactoryBean<MessageConsumeRecordMapper> factoryBean = new MapperFactoryBean<>(
                MessageConsumeRecordMapper.class
        );
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }

    /**
     * 创建通用消息消费幂等服务。 <p>
     * Mapper 通过方法参数在实例化阶段解析（此时 Mapper 工厂 Bean 已就绪）； 消费方未
     * 自定义 {@link IMessageConsumeService} 实现时才注册；
     * consumer 标识默认取 spring.application.name，可用
     * cloudmall.rocketmq.consumer 覆盖。
     *
     * @param messageConsumeRecordMapper 消费幂等台账 Mapper
     * @param properties MQ 配置
     * @param environment 环境（读取 spring.application.name）
     * @return 消息消费幂等服务
     */
    @Bean
    @ConditionalOnMissingBean(IMessageConsumeService.class)
    public MessageConsumeService messageConsumeService(
            MessageConsumeRecordMapper messageConsumeRecordMapper,
            MqProperties properties,
            Environment environment
    ) {
        String consumer = StrUtil.blankToDefault(properties.getConsumer(), appName(environment));
        return new MessageConsumeService(messageConsumeRecordMapper, consumer);
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
