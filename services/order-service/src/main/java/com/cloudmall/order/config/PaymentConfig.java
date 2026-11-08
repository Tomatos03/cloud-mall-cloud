package com.cloudmall.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 订单支付配置注册。
 *
 * @author : Tomatos
 */
@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentConfig {
}
