package com.cloudmall.mockpayment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回调通知投递状态。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum NotifyStatusEnum {
    /** 待投递 */
    PENDING("PENDING", "待投递"),

    /** 投递成功 */
    SUCCESS("SUCCESS", "投递成功"),

    /** 投递失败 */
    FAILED("FAILED", "投递失败");
    /** 状态编码 */
    private final String code;
    /** 状态描述 */
    private final String desc;
}
