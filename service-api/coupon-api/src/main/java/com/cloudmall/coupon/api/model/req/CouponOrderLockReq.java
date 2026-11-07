package com.cloudmall.coupon.api.model.req;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 优惠券锁定请求
 * <p>
 * 内部接口，由订单服务创建订单时调用：先重新计算并锁定用户选择的用户券。
 * 幂等键为订单号，重复 lock 返回上一次锁定结果。
 *
 * @author : Tomatos
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CouponOrderLockReq extends CouponOrderQuoteReq {

    /** 锁定订单号（幂等键） */
    @NotNull
    private String orderNo;

    /** 锁定过期时间（订单支付超时时间，超时后允许释放） */
    @NotNull
    private LocalDateTime lockExpireTime;
}
