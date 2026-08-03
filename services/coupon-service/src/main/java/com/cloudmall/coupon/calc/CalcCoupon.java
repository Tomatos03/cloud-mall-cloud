package com.cloudmall.coupon.calc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.enums.CouponOwnerTypeEnum;
import com.cloudmall.coupon.enums.CouponTypeEnum;

/**
 * 参与计算的一张券
 * <p>
 * 由调用方组装：规则参数来自用户券快照（发布后冻结），
 * applicableItems 是范围匹配（含店铺过滤）后的商品明细。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalcCoupon {

    /** 用户券 ID */
    private Long userCouponId;

    /** 模板 ID */
    private Long couponId;

    /** 券名称 */
    private String name;

    /** 券归属 */
    private CouponOwnerTypeEnum ownerType;

    /** 店铺 ID（平台券为 null） */
    private Long shopId;

    /** 券类型 */
    private CouponTypeEnum type;

    /** 使用门槛（0 表示无门槛） */
    private BigDecimal threshold;

    /** 满减/立减优惠金额 */
    private BigDecimal discountAmount;

    /** 折扣券减免比例 */
    private BigDecimal discountRate;

    /** 过期时间（推荐组合同优惠时优先更早过期） */
    private LocalDateTime expireTime;

    /** 发放时间（推荐组合同优惠同过期时优先更早发放） */
    private LocalDateTime createTime;

    /** 范围内商品明细（已做店铺过滤与范围匹配） */
    private List<CouponCalcItem> applicableItems;

    /**
     * 归属是否为平台券。
     *
     * @return true 表示平台券
     */
    public boolean isPlatform() {
        return CouponOwnerTypeEnum.PLATFORM.equals(this.ownerType);
    }

    /**
     * 归属是否为店铺券。
     *
     * @return true 表示店铺券
     */
    public boolean isShop() {
        return CouponOwnerTypeEnum.SHOP.equals(this.ownerType);
    }

    /**
     * 券类型是否为折扣券。
     *
     * @return true 表示折扣券
     */
    public boolean isDiscount() {
        return CouponTypeEnum.DISCOUNT.equals(this.type);
    }

    /**
     * 门槛是否满足（threshold 为空或 0 表示无门槛）。
     *
     * @param applicableAmount 适用金额
     * @return true 表示满足
     */
    public boolean isMeetsThreshold(BigDecimal applicableAmount) {
        boolean noThreshold = this.threshold == null || this.threshold.signum() <= 0;
        return noThreshold || applicableAmount.compareTo(this.threshold) >= 0;
    }

    /**
     * 是否可用：范围内商品非空且满足门槛。
     *
     * @return true 表示可用
     */
    public boolean isUsable() {
        return CollUtil.isNotEmpty(this.applicableItems) && isMeetsThreshold(sumLineAmount());
    }

    /**
     * 累加范围内商品行金额。
     *
     * @return 合计
     */
    private BigDecimal sumLineAmount() {
        if (CollUtil.isEmpty(this.applicableItems)) {
            return BigDecimal.ZERO;
        }
        return this.applicableItems.stream()
                                   .map(CouponCalcItem::getLineAmount)
                                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
