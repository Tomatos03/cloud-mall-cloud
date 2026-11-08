package com.cloudmall.payment.api.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信支付交易状态。
 * <p>
 * 对齐微信支付 API v3 查询订单接口的 trade_state 枚举。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum TradeStateEnum {
    /** 支付成功 */
    SUCCESS("SUCCESS", "支付成功"),

    /** 转入退款 */
    REFUND("REFUND", "转入退款"),

    /** 未支付 */
    NOTPAY("NOTPAY", "未支付"),

    /** 已关闭 */
    CLOSED("CLOSED", "已关闭"),

    /** 已撤销（仅付款码支付） */
    REVOKED("REVOKED", "已撤销"),

    /** 用户支付中（仅付款码支付） */
    USERPAYING("USERPAYING", "用户支付中"),

    /** 支付失败（仅付款码支付） */
    PAYERROR("PAYERROR", "支付失败");
    /** 微信支付状态码 */
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 按微信支付状态码查找枚举。
     *
     * @param code 状态码
     * @return 匹配枚举
     */
    public static TradeStateEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(value -> value.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid trade state: " + code));
    }
    /**
     * 是否支付成功。
     *
     * @return true 表示支付成功
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
