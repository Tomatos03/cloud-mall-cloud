package com.cloudmall.coupon.convert;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.api.response.CouponCandidateResp;
import com.cloudmall.coupon.api.response.CouponUsageResult;
import com.cloudmall.coupon.calc.CalcCoupon;
import com.cloudmall.coupon.entity.UserCouponDO;

/**
 * 订单协同计算转换器
 * <p>
 * 负责用户券快照到计算券、候选券、使用券快照的装配。
 * applicableItems 由范围匹配器产出，作为入参传入。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CouponOrderConverter {

    /**
     * 用户券快照组装计算券。
     *
     * @param uc 用户券
     * @param applicableItems 范围内商品明细
     * @return 计算券
     */
    @Mapping(target = "shopId", source = "uc.shopId")
    @Mapping(target = "userCouponId", source = "uc.id")
    @Mapping(target = "couponId", source = "uc.couponId")
    @Mapping(target = "name", source = "uc.nameSnapshot")
    @Mapping(target = "type", source = "uc.typeSnapshot")
    @Mapping(target = "ownerType", source = "uc.ownerType")
    @Mapping(target = "createTime", source = "uc.createTime")
    @Mapping(target = "expireTime", source = "uc.expireTime")
    @Mapping(target = "threshold", source = "uc.thresholdSnapshot")
    @Mapping(target = "applicableItems", source = "applicableItems")
    @Mapping(target = "discountRate", source = "uc.discountRateSnapshot")
    @Mapping(target = "discountAmount", source = "uc.discountAmountSnapshot")
    CalcCoupon toCalcCoupon(UserCouponDO uc, List<CouponCalcItem> applicableItems);

    /**
     * 用户券快照组装候选券。
     *
     * @param uc 用户券
     * @param applicableAmount 适用金额
     * @param estimatedDiscountAmount 预计优惠金额
     * @param valid 是否可用
     * @param invalidReason 不可用原因
     * @return 候选券
     */
    @Mapping(target = "valid", source = "valid")
    @Mapping(target = "shopId", source = "uc.shopId")
    @Mapping(target = "userCouponId", source = "uc.id")
    @Mapping(target = "couponId", source = "uc.couponId")
    @Mapping(target = "name", source = "uc.nameSnapshot")
    @Mapping(target = "type", source = "uc.typeSnapshot")
    @Mapping(target = "ownerType", source = "uc.ownerType")
    @Mapping(target = "expireTime", source = "uc.expireTime")
    @Mapping(target = "invalidReason", source = "invalidReason")
    @Mapping(target = "effectiveTime", source = "uc.effectiveTime")
    @Mapping(target = "scopeType", source = "uc.scopeTypeSnapshot")
    @Mapping(target = "threshold", source = "uc.thresholdSnapshot")
    @Mapping(target = "applicableAmount", source = "applicableAmount")
    @Mapping(target = "discountRate", source = "uc.discountRateSnapshot")
    @Mapping(target = "discountAmount", source = "uc.discountAmountSnapshot")
    @Mapping(target = "estimatedDiscountAmount", source = "estimatedDiscountAmount")
    CouponCandidateResp toCandidate(
            UserCouponDO uc,
            BigDecimal applicableAmount,
            BigDecimal estimatedDiscountAmount,
            Boolean valid,
            String invalidReason
    );

    /**
     * 计算券组装使用券快照。
     *
     * @param coupon 计算券
     * @param applicableAmount 适用金额
     * @param discount 优惠金额
     * @return 使用券快照
     */
    @Mapping(target = "discountAmount", source = "discount")
    @Mapping(target = "applicableAmount", source = "applicableAmount")
    @Mapping(target = "thresholdSnapshot", source = "coupon.threshold")
    @Mapping(target = "discountRateSnapshot", source = "coupon.discountRate")
    @Mapping(target = "discountAmountSnapshot", source = "coupon.discountAmount")
    CouponUsageResult toUsage(CalcCoupon coupon, BigDecimal applicableAmount, BigDecimal discount);
}
