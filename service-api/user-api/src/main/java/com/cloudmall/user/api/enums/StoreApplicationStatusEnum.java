package com.cloudmall.user.api.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商家入驻申请状态。
 * <p>
 * 对应 cm_store_application.status 列；NONE 仅用于接口响应，表示当前用户尚未提交申请。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum StoreApplicationStatusEnum {
    /** 未提交 */
    NONE("NONE", "未提交"),

    /** 审核中 */
    PENDING("PENDING", "审核中"),

    /** 审核通过（待激活） */
    APPROVED("APPROVED", "审核通过"),

    /** 审核驳回 */
    REJECTED("REJECTED", "审核驳回"),

    /** 店铺已开通 */
    SUCCESS("SUCCESS", "店铺已开通");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找状态。
     *
     * @param code 状态编码
     * @return 匹配状态
     */
    public static StoreApplicationStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid store application status: " + code));
    }
    /**
     * 是否审核中。
     *
     * @return true 表示审核中
     */
    public boolean isPending() {
        return this == PENDING;
    }
    /**
     * 是否已开通店铺。
     *
     * @return true 表示店铺已开通
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
