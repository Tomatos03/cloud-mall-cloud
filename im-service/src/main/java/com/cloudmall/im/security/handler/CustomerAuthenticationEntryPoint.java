package com.cloudmall.im.security.handler;

import com.cloudmall.im.security.filter.ResponseWriteUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 未认证处理器
 *
 * @author : Tomatos
 * @date : 2026/2/2
 */
@Slf4j
@Component
public class CustomerAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final String FORBIDDEN = "未认证, 请先认证";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("未认证的请求{}", request.getRequestURI());
        ResponseWriteUtil.writeForbidden(response, FORBIDDEN);
    }
}
