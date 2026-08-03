package com.cloudmall.coupon.api.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠试算/锁定响应
 * <p>
 * 父订单商品优惠快照：原始金额、店铺/平台优惠、应付金额，
 * 以及子订单级、明细级结果和实际使用的券快照。
 * quote 附带候选券与推荐组合；lock 在此基础上完成用户券锁定。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderCalcResp {

    /** 父订单原始商品金额 */
    private BigDecimal totalOriginalAmount;

    /** 店铺券优惠合计 */
    private BigDecimal totalShopDiscountAmount;

    /** 平台券优惠合计 */
    private BigDecimal totalPlatformDiscountAmount;

    /** 父订单商品应付金额（不含运费） */
    private BigDecimal goodsPayableAmount;

    /** 子订单级结果 */
    private List<CouponSubOrderResult> subOrders;

    /** 实际使用的券快照 */
    private List<CouponUsageResult> usages;

    /** 合法候选券（仅 quote 返回） */
    private List<CouponCandidateResp> candidates;

    /** 推荐最大优惠组合的 userCouponId 列表（仅 quote 返回，可空表示不使用券） */
    private List<Long> recommendedUserCouponIds;
}
