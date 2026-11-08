package com.cloudmall.payment.api.util;

import java.util.HashMap;
import java.util.Map;

import com.cloudmall.payment.api.constant.WechatPayConstants;

/**
 * 微信支付 API v3 请求 Authorization 头。
 * <p>
 * 解析 {@code WECHATPAY2-SHA256-RSA2048 mchid="...",nonce_str="...",signature="...",
 * timestamp="...",serial_no="..."} 格式，供模拟服务校验商户请求签名。
 *
 * @param mchid 商户号
 * @param nonceStr 请求随机串
 * @param signature 签名值
 * @param timestamp 请求时间戳（秒）
 * @param serialNo 商户证书序列号
 * @author : Tomatos
 */
public record WechatPayAuthorization(
    String mchid,
    String nonceStr,
    String signature,
    String timestamp,
    String serialNo
) {

    /**
     * 解析 Authorization 请求头。
     *
     * @param header Authorization 请求头
     * @return 解析结果
     */
    public static WechatPayAuthorization parse(String header) {
        if (header == null || !header.startsWith(WechatPayConstants.AUTH_SCHEME + " ")) {
            throw new IllegalArgumentException("Authorization 认证类型不合法");
        }
        String payload = header.substring(WechatPayConstants.AUTH_SCHEME.length()).trim();
        Map<String, String> values = new HashMap<>();
        for (String pair : payload.split(",")) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = parts[0].trim();
            String value = parts[1].trim();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            values.put(key, value);
        }
        return new WechatPayAuthorization(
                values.get("mchid"),
                values.get("nonce_str"),
                values.get("signature"),
                values.get("timestamp"),
                values.get("serial_no")
        );
    }
}
