package com.cloudmall.common.config;

import com.cloudmall.common.filter.AuthUserContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
public class CommonAutoConfiguration {

    @Bean
    public FilterRegistrationBean<AuthUserContextFilter> authUserContextFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<AuthUserContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthUserContextFilter(objectMapper));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
