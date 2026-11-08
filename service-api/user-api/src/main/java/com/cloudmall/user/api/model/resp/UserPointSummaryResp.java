package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分汇总。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointSummaryResp {

    private Integer balance;

    private Integer totalEarned;

    private Integer totalSpent;
}
