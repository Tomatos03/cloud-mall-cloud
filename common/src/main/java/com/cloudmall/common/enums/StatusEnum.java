package com.cloudmall.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 * <p>
 * {@code 0} 表示关闭/禁用，{@code 1} 表示开启/启用。
 * {@link EnumValue @EnumValue} 保证持久层按 {@link #value} 数值落库与查询。
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    DISABLED(0, "关闭"),
    ENABLED(1, "开启");
    @EnumValue
    private final int value;
    private final String desc;
}
