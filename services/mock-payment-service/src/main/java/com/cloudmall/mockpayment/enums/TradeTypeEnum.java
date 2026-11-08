package com.cloudmall.mockpayment.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模拟支付交易类型。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum TradeTypeEnum {
    /** 公众号/小程序支付 */
    JSAPI("JSAPI", "JSAPI支付"),

    /** 扫码支付 */
    NATIVE("NATIVE", "Native支付");
    /** 交易类型编码 */
    private final String code;
    /** 交易类型描述 */
    private final String desc;
    /**
     * 按编码查找交易类型。
     *
     * @param code 编码
     * @return 匹配枚举
     */
    public static TradeTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(value -> value.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid trade type: " + code));
    }
}
