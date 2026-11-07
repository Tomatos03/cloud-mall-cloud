package com.cloudmall.seckill.api.model.req;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀商品提交请求
 * <p>
 * 商家按 SKU 报名；product_id、shop_id、原价由服务端经 product-service 解析，
 * 不接受客户端传入，避免越权与价格伪造。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityProductSubmitReq {

    /** SKU ID（下单粒度） */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /** 秒杀价（须低于 SKU 原价） */
    @NotNull(message = "秒杀价格不能为空")
    @Positive(message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    /** 秒杀库存（不得超过 SKU 库存） */
    @NotNull(message = "秒杀库存不能为空")
    @Positive(message = "秒杀库存必须大于0")
    private Integer stock;
}
