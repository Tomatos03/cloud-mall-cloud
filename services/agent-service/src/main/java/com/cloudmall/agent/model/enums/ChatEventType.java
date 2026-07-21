package com.cloudmall.agent.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ChatEventType {
    MESSAGE("消息", 1),
    DONE("结束", 2);

    private final String desc;
    @JsonValue
    private final int code;

    ChatEventType(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }
}
