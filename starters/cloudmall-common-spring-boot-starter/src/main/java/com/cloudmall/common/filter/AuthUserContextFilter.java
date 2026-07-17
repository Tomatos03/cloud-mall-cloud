package com.cloudmall.common.filter;

import java.io.IOException;

import com.cloudmall.common.context.AuthUserContext;
import com.cloudmall.common.context.AuthUserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class AuthUserContextFilter extends OncePerRequestFilter {

    private static final String AUTH_USER_HEADER = "X-Auth-User";

    private final ObjectMapper objectMapper;

    public AuthUserContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(AUTH_USER_HEADER);
        if (header != null && !header.isEmpty()) {
            AuthUserContext context = objectMapper.readValue(header, AuthUserContext.class);
            AuthUserContextHolder.set(context);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            AuthUserContextHolder.clear();
        }
    }
}
