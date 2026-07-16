package com.cloudmall.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.cloudmall.user.mapper")
public class UserDataSourceConfig {
}
