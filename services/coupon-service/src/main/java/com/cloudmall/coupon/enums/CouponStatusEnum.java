package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券模板状态枚举
 * <p>
 * 对应 cm_coupon.status 列的取值集合。库存耗尽不改变模板状态，
 * 只派生 claimable = false、stock_remaining = 0。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponStatusEnum {

    /** 草稿 — 可编辑，未发放草稿允许软删除 */
    DRAFT("DRAFT", "草稿"),

    /** 已发布 — 可在发放窗口内领取或自动发放 */
    ACTIVE("ACTIVE", "已发布"),

    /** 已暂停 — 停止新发放，已发放用户券不受影响，可恢复 */
    PAUSED("PAUSED", "已暂停"),

    /** 已结束 — 停止新发放，不可恢复 */
    ENDED("ENDED", "已结束");

    /** 数据库存储值 */
    @EnumValue
    private final String code;

    /** 状态描述 */
    private final String desc;

    /**
     * 根据编码查找模板状态。
     *
     * @param code 状态编码，如 "ACTIVE"
     * @return 匹配的模板状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static CouponStatusEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(t -> t.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(
                             "Invalid coupon status: '" + code + "', valid values: "
                                     + Arrays.toString(values())
                     ));
    }
}
