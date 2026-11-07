package com.cloudmall.coupon.api.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按订单回退用户券请求
 * <p>
 * 内部接口，由消费服务在父订单全额退款事件中调用：
 * 回退该订单全部已核销（USED）的用户券，幂等键为订单号 + 退款单号。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderRevertByOrderReq {

    /** 父订单号（幂等键） */
    @NotBlank
    private String orderNo;

    /** 退款单 ID（幂等键） */
    @NotBlank
    private String refundId;
}
