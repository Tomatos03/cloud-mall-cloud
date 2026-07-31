package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户券发放来源枚举
 * <p>
 * 对应 cm_user_coupon.issued_source 列的取值集合。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum IssuedSourceEnum {

    /** 公开领取 — 用户从券中心主动领取 */
    PUBLIC_CLAIM("PUBLIC_CLAIM", "公开领取"),

    /** 注册自动发放 — 注册事件触发的平台新人券 */
    REGISTER_AUTO("REGISTER_AUTO", "注册自动发放");

    /** 数据库存储值 */
    @EnumValue
    private final String code;

    /** 来源描述 */
    private final String desc;

    /**
     * 根据编码查找发放来源。
     *
     * @param code 来源编码，如 "PUBLIC_CLAIM"
     * @return 匹配的发放来源
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static IssuedSourceEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(t -> t.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(
                             "Invalid issued source: '" + code + "', valid values: "
                                     + Arrays.toString(values())
                     ));
    }
}
