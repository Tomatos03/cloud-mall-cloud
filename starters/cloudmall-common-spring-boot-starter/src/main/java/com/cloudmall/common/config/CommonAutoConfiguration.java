package com.cloudmall.common.config;

import com.cloudmall.common.filter.UserContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
public class CommonAutoConfiguration {

    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<UserContextFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new UserContextFilter(objectMapper));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }
}
