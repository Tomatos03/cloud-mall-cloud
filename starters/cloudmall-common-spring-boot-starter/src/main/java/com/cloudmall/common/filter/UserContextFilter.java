package com.cloudmall.common.filter;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserContextFilter extends OncePerRequestFilter {

    private static final String X_AUTH_USER = "X-Auth-User";

    private final ObjectMapper objectMapper;

    public UserContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(X_AUTH_USER);
        initUserContext(header);
        try {
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private void initUserContext(String header) throws JsonProcessingException {
        if (header != null && !header.isEmpty()) {
            UserContext ctx = objectMapper.readValue(header, UserContext.class);
            UserContextHolder.set(ctx);
        }
    }
}
