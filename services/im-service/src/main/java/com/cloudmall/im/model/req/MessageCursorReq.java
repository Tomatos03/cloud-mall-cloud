package com.cloudmall.im.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.model.CursorReq;

/**
 * 消息历史游标查询请求
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCursorReq extends CursorReq {

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
}
