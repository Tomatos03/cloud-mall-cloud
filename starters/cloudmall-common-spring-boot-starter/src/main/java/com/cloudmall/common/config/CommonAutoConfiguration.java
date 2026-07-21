package com.cloudmall.common.config;

import com.cloudmall.common.feign.FeignUserContextInterceptor;
import com.cloudmall.common.filter.UserContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonAutoConfiguration {

    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<UserContextFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new UserContextFilter(objectMapper));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }

    @Configuration
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    static class FeignConfiguration {

        @Bean
        FeignUserContextInterceptor feignUserContextInterceptor(ObjectMapper objectMapper) {
            return new FeignUserContextInterceptor(objectMapper);
        }
    }
}
