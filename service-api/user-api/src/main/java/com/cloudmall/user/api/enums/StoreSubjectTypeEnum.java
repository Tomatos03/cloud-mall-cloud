package com.cloudmall.user.api.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商家入驻主体类型。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum StoreSubjectTypeEnum {
    /** 个人店 */
    PERSONAL("personal", "个人店"),

    /** 个体工商户 */
    INDIVIDUAL("individual", "个体店"),

    /** 企业店 */
    ENTERPRISE("enterprise", "企业店");
    /** 数据库存储值（JSON 也输出编码，便于前端直接使用） */
    @EnumValue
    @JsonValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找主体类型。
     *
     * @param code 类型编码
     * @return 匹配类型
     */
    public static StoreSubjectTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(value -> value.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid store subject type: " + code));
    }
    /**
     * 是否需要营业执照。
     *
     * @return true 表示需要营业执照
     */
    public boolean requiresLicense() {
        return this != PERSONAL;
    }
}
