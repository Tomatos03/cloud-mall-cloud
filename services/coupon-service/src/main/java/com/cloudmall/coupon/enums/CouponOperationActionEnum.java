package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券模板操作类型枚举
 * <p>
 * 对应 cm_coupon_operation_log.action_type 列的取值集合，
 * 记录模板管理操作的审计流水。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum CouponOperationActionEnum {
    /** 创建 — 创建模板草稿 */
    CREATE("CREATE", "创建"),

    /** 编辑 — 仅草稿可编辑 */
    UPDATE("UPDATE", "编辑"),

    /** 发布 — DRAFT 或 PAUSED 进入 ACTIVE */
    PUBLISH("PUBLISH", "发布"),

    /** 暂停 — ACTIVE 进入 PAUSED */
    PAUSE("PAUSE", "暂停"),

    /** 恢复 — PAUSED 回到 ACTIVE */
    RESUME("RESUME", "恢复"),

    /** 结束 — 进入 ENDED，不可恢复 */
    END("END", "结束"),

    /** 删除 — 仅未发放草稿可删除 */
    DELETE("DELETE", "删除"),

    /** 库存调整 — 调整发行库存 */
    STOCK_ADJUST("STOCK_ADJUST", "库存调整"),

    /** 紧急撤销 — 运营撤销未锁定用户券 */
    REVOKE("REVOKE", "紧急撤销");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 操作描述 */
    private final String desc;
    /**
     * 根据编码查找模板操作类型。
     *
     * @param code 操作编码，如 "PUBLISH"
     * @return 匹配的操作类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static CouponOperationActionEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid coupon operation action: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
