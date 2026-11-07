package com.cloudmall.order.api.model.resp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 父订单响应
 * <p>
 * 聚合店铺子订单、明细和实际使用的券快照，供用户查看完整订单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResp {

    /** 父订单 ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 商品原价总额 */
    private BigDecimal totalOriginalAmount;

    /** 店铺券优惠总额 */
    private BigDecimal totalShopDiscountAmount;

    /** 平台券优惠总额 */
    private BigDecimal totalPlatformDiscountAmount;

    /** 商品应付金额 */
    private BigDecimal productPayableAmount;

    /** 运费金额 */
    private BigDecimal freightAmount;

    /** 最终应付金额 */
    private BigDecimal payableAmount;

    /** 订单状态 code */
    private String status;

    /** 收货地址快照（JSON） */
    private String addressSnapshot;

    /** 订单备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 店铺子订单列表 */
    private List<OrderSubResp> subs;

    /** 实际使用的券快照列表 */
    private List<OrderCouponResp> coupons;
}
