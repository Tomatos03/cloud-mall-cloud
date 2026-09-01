package com.cloudmall.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import com.cloudmall.common.filter.UserContextFilter;
import com.cloudmall.common.handler.GlobalExceptionHandler;

@AutoConfiguration
@Import(GlobalExceptionHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonAutoConfiguration {

    /**
     * 注册用户上下文过滤器。
     *
     * @param objectMapper JSON 对象映射器
     * @return 处理结果
     */
    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<UserContextFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new UserContextFilter(objectMapper));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }
}
