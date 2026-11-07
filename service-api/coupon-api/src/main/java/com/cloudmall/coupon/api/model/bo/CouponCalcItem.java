package com.cloudmall.coupon.api.model.bo;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可信商品明细
 * <p>
 * 由订单服务从商品服务批量查询组装后传入，coupon-service 不信任客户端价格。
 * lineAmount = unitPrice × quantity，由调用方计算。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCalcItem {

    /** SKU ID */
    @NotNull
    private Long skuId;

    /** SPU ID */
    @NotNull
    private Long productId;

    /** 商品当前分类 ID */
    @NotNull
    private Long categoryId;

    /** 商品所属店铺 ID */
    @NotNull
    private Long shopId;

    /** 当前可信销售价 */
    @NotNull
    @DecimalMin("0")
    private BigDecimal unitPrice;

    /** 购买数量 */
    @Min(1)
    @NotNull
    private Integer quantity;

    /** 行金额（unitPrice × quantity） */
    @NotNull
    @DecimalMin("0")
    private BigDecimal lineAmount;
}
