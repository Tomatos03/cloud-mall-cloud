package com.cloudmall.order.api.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀订单创建请求（内部接口）
 * <p>
 * 由 consumer-service 消费秒杀建单事件后调用，跳过购物车、优惠券与普通库存预占。
 * orderNo 由秒杀侧预生成并传入，配合 cm_order.uk_order_no 保证重复投递幂等。
 * 商品名称、店铺、分类等展示字段由 order-service 从 SKU 可信快照取，成交价固定为秒杀价。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderCreateReq {

    /** 订单号（秒杀侧预生成，幂等键） */
    @NotBlank
    private String orderNo;

    /** 下单用户 ID */
    @NotNull
    private Long userId;

    /** 收货地址 ID */
    @NotNull
    private Long addressId;

    /** SKU ID */
    @NotNull
    private Long skuId;

    /** 商品 SPU ID */
    @NotNull
    private Long productId;

    /** 秒杀成交单价 */
    @NotNull
    private BigDecimal seckillPrice;

    /** 购买数量（固定 1） */
    @NotNull
    private Integer quantity;
}
