package com.cloudmall.aigc.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResp {
    private String role;
    private String content;

    /** 助手回复对应的工具卡片，普通文本消息为 null */
    private List<AgentCardResp> cards;
}
