package com.cloudmall.seckill.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀商品审核状态枚举
 * <p>
 * 对应 cm_seckill_product.audit_status 列；REJECTED 后商家可修改重提回到 PENDING。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SeckillProductAuditStatusEnum {
    /** 待审核 */
    PENDING("PENDING", "待审核"),

    /** 已通过 — 参与预热与抢购 */
    APPROVED("APPROVED", "已通过"),

    /** 已驳回 — 可修改后重新提交 */
    REJECTED("REJECTED", "已驳回");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找审核状态。
     *
     * @param code 状态编码，如 "APPROVED"
     * @return 匹配的审核状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static SeckillProductAuditStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid seckill product audit status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
