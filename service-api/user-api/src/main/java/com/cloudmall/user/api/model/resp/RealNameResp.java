package com.cloudmall.user.api.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前用户实名认证信息。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealNameResp {

    private String status;

    private String realNameMasked;

    private String idCardMasked;

    private String rejectReason;

    private LocalDateTime submitTime;

    private LocalDateTime verifyTime;
}
