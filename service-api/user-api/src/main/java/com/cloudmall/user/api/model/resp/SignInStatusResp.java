package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日签到状态。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInStatusResp {

    private Boolean signedToday;

    private Integer streakDays;

    private Integer todayReward;

    private Integer balance;
}
