package com.cloudmall.im.security.filter;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cloudmall.im.security.JwtTokenProvider;
import com.cloudmall.im.security.ParsedToken;
import com.cloudmall.im.security.context.AuthUserContext;

/**
 * Token 认证过滤器
 *
 * @author : Tomatos
 * @date : 2026/2/3
 */
@Slf4j
@Component
public class IMTokenAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        log.info("Method[{}], URI: {}", request.getMethod(), requestUri);
        try {
            String token;
            ParsedToken parsedToken;
            if (
                    (token = getTokenFromRequest(request)) != null
                            && (parsedToken = jwtTokenProvider.parse(token)) != null
            ) {
                UsernamePasswordAuthenticationToken authenticatedToken =
                        new UsernamePasswordAuthenticationToken(
                                parsedToken,
                                "",
                                Collections.emptyList()
                        );
                AuthUserContext.setAuthentication(authenticatedToken);
            }
        } catch (Exception e) {
            log.error("解析Token异常: {}", e.getMessage());
            ResponseWriteUtil.writeUnauthorized(response, "Token过期");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return request.getParameter("token");
    }
}