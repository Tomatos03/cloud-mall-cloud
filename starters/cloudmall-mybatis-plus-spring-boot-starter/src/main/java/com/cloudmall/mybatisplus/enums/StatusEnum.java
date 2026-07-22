package com.cloudmall.mybatisplus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 * 0=关闭/禁用  1=开启/启用
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
