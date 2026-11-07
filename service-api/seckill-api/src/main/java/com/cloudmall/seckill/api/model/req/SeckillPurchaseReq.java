package com.cloudmall.seckill.api.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀抢购请求
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillPurchaseReq {

    /** 活动 ID */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    /** SKU ID */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /** 收货地址 ID */
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
}
