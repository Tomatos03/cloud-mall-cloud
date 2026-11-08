package com.cloudmall.user.api.model.req;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付积分发放请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPointAwardReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;
}
