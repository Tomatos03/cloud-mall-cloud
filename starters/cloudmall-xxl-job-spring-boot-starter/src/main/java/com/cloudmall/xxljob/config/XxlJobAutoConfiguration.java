package com.cloudmall.xxljob.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.cloudmall.xxljob.properties.XxlJobProperties;

import com.cloudmall.xxljob.register.XxlJobAutoRegister;

/**
 * XXL-JOB 执行器自动配置。
 *
 * @author : Tomatos
 */
@AutoConfiguration
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration {

    /**
     * 创建执行器：负责注册 @XxlJob 处理器、与调度中心心跳、接收调度触发。
     *
     * @param properties 配置属性
     * @return 执行器
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobSpringExecutor(XxlJobProperties properties) {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdmin().getAddresses());
        executor.setAccessToken(properties.getAccessToken());
        executor.setAppname(properties.getExecutor().getAppName());
        executor.setIp(properties.getExecutor().getIp());
        executor.setPort(properties.getExecutor().getPort());
        executor.setLogPath(properties.getExecutor().getLogPath());
        executor.setLogRetentionDays(properties.getExecutor().getLogRetentionDays());
        return executor;
    }

    /**
     * 创建任务自动注册器（启动时调用调度中心 API，幂等注册声明式任务）。
     *
     * @param properties 配置属性
     * @return 自动注册器
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobAutoRegister xxlJobAutoRegister(XxlJobProperties properties) {
        return new XxlJobAutoRegister(properties);
    }
}
