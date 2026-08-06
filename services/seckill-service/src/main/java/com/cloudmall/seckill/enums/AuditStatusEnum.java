package com.cloudmall.seckill.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核单状态枚举
 * <p>
 * 对应 cm_audit_record.status 列。同一审核对象同一时刻最多一条 PENDING，
 * 重新提交时原 PENDING 单先置为 WITHDRAWN。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {
    /** 待审核 */
    PENDING("PENDING", "待审核"),

    /** 已通过 */
    APPROVED("APPROVED", "已通过"),

    /** 已驳回 */
    REJECTED("REJECTED", "已驳回"),

    /** 已撤回 — 商家重新提交时作废旧单 */
    WITHDRAWN("WITHDRAWN", "已撤回");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找审核单状态。
     *
     * @param code 状态编码，如 "PENDING"
     * @return 匹配的审核单状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static AuditStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid audit status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
