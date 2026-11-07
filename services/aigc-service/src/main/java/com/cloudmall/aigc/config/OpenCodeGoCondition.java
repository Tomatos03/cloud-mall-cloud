package com.cloudmall.aigc.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * OpenCode Go 启用条件。
 * <p>
 * 同时要求 {@code agent.opencode-go.enabled=true} 且配置了 API Key；
 * 密钥缺失时跳过该模型，让应用回退到 OpenAI 聊天模型而不是启动失败。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
public class OpenCodeGoCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Boolean enabled = context.getEnvironment()
                                 .getProperty("agent.opencode-go.enabled", Boolean.class, false);
        String apiKey = context.getEnvironment().getProperty("agent.opencode-go.api-key");
        return Boolean.TRUE.equals(enabled) && StrUtil.isNotBlank(apiKey);
    }
}
