package com.cloudmall.im.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建会话结果
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResp {

    /** 会话ID */
    private Long sessionId;
}
