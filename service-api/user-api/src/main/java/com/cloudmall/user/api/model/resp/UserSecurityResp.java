package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前用户账户安全信息。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityResp {

    private Boolean payPasswordSet;

    private String realNameStatus;

    private String realNameMasked;

    private String idCardMasked;

    private String rejectReason;

    private java.time.LocalDateTime submitTime;

    private java.time.LocalDateTime verifyTime;
}
