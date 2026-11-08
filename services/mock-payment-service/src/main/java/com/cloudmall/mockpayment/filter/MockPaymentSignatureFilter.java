package com.cloudmall.mockpayment.filter;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cloudmall.common.exception.BizException;
import com.cloudmall.mockpayment.model.resp.WechatErrorResp;
import com.cloudmall.mockpayment.properties.MockPaymentProperties;
import com.cloudmall.mockpayment.service.impl.WechatPaySignatureService;

/**
 * 微信支付 v3 请求验签过滤器。
 * <p>
 * 仅拦截 /v3/** 协议接口，缓存原始请求体后交给签名服务校验；
 * 校验失败直接返回微信支付风格 401 错误，不进入业务控制器。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class MockPaymentSignatureFilter extends OncePerRequestFilter {

    private final MockPaymentProperties properties;
    private final WechatPaySignatureService signatureService;
    private final ObjectMapper objectMapper;

    /**
     * 判断是否跳过当前请求。
     *
     * @param request 请求
     * @return true 表示跳过验签
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.getSignature().isVerifyRequest() || !request.getRequestURI().startsWith("/v3/");
    }

    /**
     * 执行验签。
     *
     * @param request 请求
     * @param response 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        byte[] body = CachedBodyHttpServletRequest.readBody(request);
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request, body);
        try {
            signatureService.verifyRequest(
                    request.getHeader("Authorization"),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    new String(body, java.nio.charset.StandardCharsets.UTF_8)
            );
        } catch (BizException | IllegalArgumentException e) {
            log.warn("模拟支付请求验签失败: {} {}", request.getMethod(), request.getRequestURI());
            writeError(response, e.getMessage());
            return;
        }
        filterChain.doFilter(cachedRequest, response);
    }

    /**
     * 写出微信支付风格错误响应。
     *
     * @param response 响应
     * @param message 错误信息
     * @throws IOException IO 异常
     */
    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                new WechatErrorResp("SIGN_ERROR", message == null ? "签名错误" : message)
        );
    }
}
