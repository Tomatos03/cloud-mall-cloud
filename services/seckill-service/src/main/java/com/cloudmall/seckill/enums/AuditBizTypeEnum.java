package com.cloudmall.seckill.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核对象类型枚举
 * <p>
 * 对应 cm_audit_record.biz_type 列；本期仅秒杀商品审核场景，后续可扩展。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum AuditBizTypeEnum {
    /** 秒杀商品报名审核 */
    SECKILL_PRODUCT("SECKILL_PRODUCT", "秒杀商品");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找审核对象类型。
     *
     * @param code 类型编码，如 "SECKILL_PRODUCT"
     * @return 匹配的审核对象类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static AuditBizTypeEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid audit biz type: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
