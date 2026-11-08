package com.cloudmall.mockpayment.model.resp;

/**
 * 微信支付风格错误响应。
 *
 * @param code 错误码
 * @param message 错误描述
 * @author : Tomatos
 */
public record WechatErrorResp(String code, String message) {
}
