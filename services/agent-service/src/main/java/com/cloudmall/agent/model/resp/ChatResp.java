package com.cloudmall.agent.model.resp;

import com.cloudmall.agent.model.enums.ChatEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResp {
    private String data;
    private ChatEventType eventType;

    public static ChatResp end() {
        return ChatResp.builder()
                .eventType(ChatEventType.DONE)
                .build();
    }
}
