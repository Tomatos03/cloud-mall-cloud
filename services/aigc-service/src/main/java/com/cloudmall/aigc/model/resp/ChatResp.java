package com.cloudmall.aigc.model.resp;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.aigc.model.enums.ChatEventType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResp {
    private Object data;
    private ChatEventType eventType;

    /**
     * 创建聊天结束事件响应。
     *
     * @return 处理结果
     */
    public static ChatResp end() {
        return ChatResp.builder()
                .eventType(ChatEventType.DONE)
                .build();
    }

    /**
     * 创建聊天参数事件响应。
     *
     * @param params 参数映射
     * @return 处理结果
     */
    public static ChatResp param(Map<String, Object> params) {
        return ChatResp.builder()
                .data(params)
                .eventType(ChatEventType.PARAM)
                .build();
    }
}
