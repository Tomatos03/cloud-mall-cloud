package com.cloudmall.im.interceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.cloudmall.jwt.constant.JwtClaimNames;
import com.cloudmall.jwt.exception.ParseTokenException;
import com.cloudmall.jwt.model.ParsedToken;
import com.cloudmall.jwt.service.ITokenService;

/**
 * WebSocket 握手拦截器
 * <p>
 * 浏览器 WebSocket 无法携带请求头，token 通过 query 参数传递，
 * 使用共享 {@link ITokenService} 校验（与网关同一套 JWT 密钥）。
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final ITokenService tokenService;

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        String token = extractTokenFromQuery(request.getURI().getQuery());
        if (StrUtil.isBlank(token)) {
            log.warn("WebSocket 握手失败：未找到 token 参数");
            return false;
        }
        try {
            ParsedToken parsedToken = tokenService.parseToken(token);
            Long userId = parsedToken.getLong(JwtClaimNames.USER_ID);
            attributes.put("userId", userId);
            log.info("WebSocket 握手成功，userId: {}", userId);
            return true;
        } catch (ParseTokenException e) {
            log.warn("WebSocket 握手失败：token 无效或已过期");
            return false;
        }
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        @Nullable
        Exception exception
    ) {}

    /**
     * 从查询字符串中提取 token 值。
     *
     * @param query 查询字符串
     * @return token 值，不存在时返回 null
     */
    private String extractTokenFromQuery(String query) {
        if (query == null) {
            return null;
        }
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                try {
                    return URLDecoder.decode(param.substring("token=".length()), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.error("token URL 解码失败", e);
                    return null;
                }
            }
        }
        return null;
    }
}
