package com.cloudmall.common.feign;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.context.UserContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 请求拦截器
 * 将当前线程的用户上下文写入 X-Auth-User 请求头，实现服务间用户信息透传
 *
 * @author Tomatos
 */
@Slf4j
@RequiredArgsConstructor
public class FeignUserContextInterceptor implements RequestInterceptor {

    private static final String HEADER = "X-Auth-User";

    private final ObjectMapper objectMapper;

    @Override
    public void apply(RequestTemplate template) {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null) {
            return;
        }

        // 如果已有该头（网关传递过来的），不再覆盖
        if (template.headers().containsKey(HEADER)) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(ctx);
            template.header(HEADER, json);
        } catch (JsonProcessingException e) {
            log.warn("序列化 UserContext 失败", e);
        }
    }
}
