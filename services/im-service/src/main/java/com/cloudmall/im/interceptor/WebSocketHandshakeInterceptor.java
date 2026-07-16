package com.cloudmall.im.interceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.cloudmall.im.security.JwtTokenProvider;
import com.cloudmall.im.security.ParsedToken;

/**
 * WebSocket握手拦截器
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        try {
            String query = request.getURI()
                                  .getQuery();
            if (query == null || !query.contains("token=")) {
                log.warn("WebSocket连接请求中未找到token参数");
                return false;
            }

            String token = extractTokenFromQuery(query);
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket token参数为空");
                return false;
            }

            log.info("WebSocket握手：开始解析token...");

            ParsedToken parsedToken = jwtTokenProvider.parse(token);
            if (parsedToken == null) {
                log.warn("WebSocket token解析失败");
                return false;
            }

            log.info("WebSocket握手：token解析成功，userId: {}, username: {}", parsedToken.getUserId(),
                     parsedToken.getUsername());

            attributes.put("userId", parsedToken.getUserId());
            log.info("WebSocket握手：认证成功");
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手认证异常:", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               @Nullable Exception exception) {
    }

    /**
     * 从查询字符串中提取token值
     */
    private String extractTokenFromQuery(String query) {
        if (query == null) return null;

        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                String token = param.substring("token=".length());
                try {
                    return URLDecoder.decode(token, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.error("token URL解码失败", e);
                    return null;
                }
            }
        }
        return null;
    }
}