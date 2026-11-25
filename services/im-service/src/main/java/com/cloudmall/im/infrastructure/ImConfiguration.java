package com.cloudmall.im.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IM 配置类。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Configuration
@EnableConfigurationProperties(ImProperties.class)
public class ImConfiguration {
}
