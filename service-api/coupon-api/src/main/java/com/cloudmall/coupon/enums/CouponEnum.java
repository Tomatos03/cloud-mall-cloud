package com.cloudmall.coupon.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型枚举
 * <p>
 * 对应 cm_coupon.type 列的取值集合，模板发布后冻结，用户券保存领取时快照。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponEnum {
    /** 满减券 — 满足门槛后按固定金额减免 */
    FULL_REDUCTION("FULL_REDUCTION", "满减") {
        @Override
        public BigDecimal calcDiscount(
                BigDecimal applicableAmount,
                BigDecimal discountAmount,
                BigDecimal discountRate,
                int scale,
                RoundingMode rounding
        ) {
            return minAmount(applicableAmount, discountAmount);
        }
    },

    /** 折扣券 — 按减免比例优惠，discount_rate 表示减免比例（0.10 表示减免 10%） */
    DISCOUNT("DISCOUNT", "折扣") {
        @Override
        public BigDecimal calcDiscount(
                BigDecimal applicableAmount,
                BigDecimal discountAmount,
                BigDecimal discountRate,
                int scale,
                RoundingMode rounding
        ) {
            return applicableAmount.multiply(discountRate).setScale(scale, rounding);
        }
    },

    /** 立减券 — 无门槛或满足门槛后按固定金额直接立减 */
    CASH("CASH", "立减") {
        @Override
        public BigDecimal calcDiscount(
                BigDecimal applicableAmount,
                BigDecimal discountAmount,
                BigDecimal discountRate,
                int scale,
                RoundingMode rounding
        ) {
            return minAmount(applicableAmount, discountAmount);
        }
    };
    /** 数据库存储值 */
    @EnumValue
    @JsonValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 按券类型规则计算优惠金额。
     *
     * @param applicableAmount 适用金额
     * @param discountAmount 满减/立减固定金额
     * @param discountRate 折扣比例
     * @param scale 小数位
     * @param rounding 舍入模式
     * @return 优惠金额
     */
    public abstract BigDecimal calcDiscount(
            BigDecimal applicableAmount,
            BigDecimal discountAmount,
            BigDecimal discountRate,
            int scale,
            RoundingMode rounding
    );
    /**
     * 取适用金额与固定优惠金额的较小值（优惠不超过应付金额）。
     *
     * @param applicableAmount 适用金额
     * @param discountAmount 固定优惠金额
     * @return 较小值
     */
    private static BigDecimal minAmount(BigDecimal applicableAmount, BigDecimal discountAmount) {
        return discountAmount.compareTo(applicableAmount) > 0 ? applicableAmount : discountAmount;
    }
    /**
     * 根据编码查找优惠券类型。
     *
     * @param code 类型编码，如 "FULL_REDUCTION"
     * @return 匹配的优惠券类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    @JsonCreator
    public static CouponEnum fromCode(@JsonProperty("code") String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid coupon type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
