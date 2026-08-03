package com.cloudmall.product.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存确认请求
 * <p>
 * 内部接口，支付成功后调用：不改库存，只记录确认流水（幂等键为订单号）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockConfirmReq {

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;
}
