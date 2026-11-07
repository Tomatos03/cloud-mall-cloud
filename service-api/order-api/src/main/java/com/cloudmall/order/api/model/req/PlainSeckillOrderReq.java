package com.cloudmall.order.api.model.req;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通秒杀同步建单请求
 * <p>
 * 由 seckill-service 在普通流程抢购时组装：秒杀价、商品快照等一律来自
 * seckill-service 侧的可信数据（活动商品 + SKU 快照），order-service
 * 不重新计算，仅按快照幂等落库真实订单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlainSeckillOrderReq {

    /** 真实订单号（seckill-service 预生成，uk_order_no 幂等键） */
    @NotBlank
    private String orderNo;

    /** 下单用户 ID */
    @NotNull
    private Long userId;

    /** 商品 SPU ID */
    @NotNull
    private Long productId;

    /** SKU ID */
    @NotNull
    private Long skuId;

    /** 店铺 ID */
    @NotNull
    private Long shopId;

    /** 商品分类 ID（可空） */
    private Long categoryId;

    /** 商品名称快照 */
    private String productName;

    /** 商品主图快照 */
    private String productImage;

    /** 规格描述快照 */
    private String specSnapshot;

    /** 秒杀成交单价 */
    @NotNull
    private BigDecimal seckillPrice;

    /** 购买数量（固定 1） */
    @NotNull
    private Integer quantity;

    /** 收货地址快照（JSON） */
    private String addressSnapshot;
}
