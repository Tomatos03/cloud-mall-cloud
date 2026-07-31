package com.cloudmall.common.filter;

import java.io.IOException;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;

public class UserContextFilter extends OncePerRequestFilter {

    private static final String X_AUTH_USER = "X-Auth-User";

    private final ObjectMapper objectMapper;

    /**
     * 使用指定参数构造对象。
     *
     * @param objectMapper JSON 对象映射器
     */
    public UserContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 处理 HTTP 请求并继续过滤链。
     *
     * @param request 请求参数
     * @param response HTTP 响应对象
     * @param chain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String header = request.getHeader(X_AUTH_USER);
        initUserContext(header);
        try {
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * 初始化用户上下文。
     *
     * @param header 用户上下文请求头
     */
    private void initUserContext(String header) throws JsonProcessingException {
        if (StrUtil.isNotBlank(header)) {
            UserContext ctx = objectMapper.readValue(header, UserContext.class);
            UserContextHolder.set(ctx);
        }
    }
}
