package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按订单核销用户券请求
 * <p>
 * 内部接口，由消费服务在支付成功事件中调用：
 * 核销该订单全部已锁定（LOCKED）的用户券，幂等。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderCommitByOrderReq {

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;
}
