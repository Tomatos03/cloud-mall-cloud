package com.cloudmall.auth.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 * <p>
 * 对应 cm_user.types 列的取值集合（MySQL SET 类型，一个用户可拥有多个类型，
 * 存储形式为逗号分隔的编码串，如 "NORMAL,MERCHANT"）。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    /** 普通用户 */
    NORMAL("NORMAL", "普通用户"),

    /** 管理员 */
    ADMIN("ADMIN", "管理员"),

    /** 店铺商家 */
    MERCHANT("MERCHANT", "店铺商家");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 类型描述 */
    private final String desc;
    /**
     * 根据编码查找用户类型。
     *
     * @param code 类型编码，如 "NORMAL"
     * @return 匹配的用户类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static UserTypeEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid user type: '" + code + "', valid values: " + allCodes()
                        )
                );
    }
    /**
     * 解析逗号分隔的编码串为用户类型集合。 <p>
     * cm_user.types 为 MySQL SET 类型，一个用户可拥有多个类型，如
     * "NORMAL,MERCHANT"。
     *
     * @param codes 逗号分隔的类型编码，如 "NORMAL,MERCHANT"；空白入参返回空
     *              集合
     * @return 用户类型集合（不保证顺序）
     */
    public static Set<UserTypeEnum> fromCodes(String codes) {
        if (StrUtil.isBlank(codes)) {
            return Collections.emptySet();
        }
        return StrUtil.split(codes, ',')
                      .stream()
                      .map(UserTypeEnum::fromCode)
                      .collect(Collectors.toSet());
    }
    /**
     * 获取全部合法类型编码。
     *
     * @return 全部类型编码列表
     */
    public static List<String> allCodes() {
        return Arrays.stream(values())
                     .map(UserTypeEnum::getCode)
                     .toList();
    }
}
