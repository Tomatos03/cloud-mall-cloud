package com.cloudmall.aigc.agent;

import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

import com.cloudmall.aigc.model.req.ChatReq;
import com.cloudmall.aigc.model.resp.ChatResp;

public interface Agent {

    /**
     * 获取智能体唯一类型标识。
     *
     * @return 智能体类型
     */
    AgentType getType();

    /**
     * 智能体职责描述（用于路由 LLM 判断）。
     * 默认委托给 {@link AgentType#getDescription()}，
     * 子类可覆写以提供更具体的描述。
     *
     * @return 智能体职责描述
     */
    default String getDescription() {
        return getType().getDescription();
    }

    /**
     * 返回该智能体独立的 ChatClient 实例。
     *
     * @return ChatClient 实例
     */
    ChatClient getChatClient();

    /**
     * 处理聊天请求，返回流式结果。
     *
     * @param req 聊天请求
     * @return 流式聊天响应
     */
    Flux<ChatResp> chat(ChatReq req);
}
