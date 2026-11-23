package com.cloudmall.shared.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件台账状态。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum FileStatusEnum {
    /** 已上传、尚未被业务引用 */
    PENDING("PENDING", "待绑定"),

    /** 已被业务引用 */
    BOUND("BOUND", "已使用"),

    /** 曾绑定但已被替换或释放，等待回收 */
    ORPHAN("ORPHAN", "待回收");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
}
