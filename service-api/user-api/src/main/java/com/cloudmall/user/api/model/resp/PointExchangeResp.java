package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分兑换结果。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointExchangeResp {

    private Long orderId;

    private Integer pointsCost;

    private Integer balance;

    private Long userCouponId;
}
