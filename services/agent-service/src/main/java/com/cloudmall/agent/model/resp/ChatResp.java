package com.cloudmall.agent.model.resp;

import com.cloudmall.agent.model.enums.ChatEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResp {
    private Object data;
    private ChatEventType eventType;

    public static ChatResp end() {
        return ChatResp.builder()
                .eventType(ChatEventType.DONE)
                .build();
    }

    public static ChatResp param(Map<String, Object> params) {
        return ChatResp.builder()
                .data(params)
                .eventType(ChatEventType.PARAM)
                .build();
    }
}
