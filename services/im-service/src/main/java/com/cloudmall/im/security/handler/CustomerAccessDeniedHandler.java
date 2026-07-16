package com.cloudmall.im.security.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.cloudmall.im.security.filter.ResponseWriteUtil;

/**
 * 未授权处理器
 *
 * @author : Tomatos
 * @date : 2026/2/2
 */
@Component
@Slf4j
public class CustomerAccessDeniedHandler implements AccessDeniedHandler {
    private static final String UNAUTHORIZED = "未授权";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.warn("未授权的请求{}", request.getRequestURI(), accessDeniedException);
        ResponseWriteUtil.writeUnauthorized(response, UNAUTHORIZED);
    }
}