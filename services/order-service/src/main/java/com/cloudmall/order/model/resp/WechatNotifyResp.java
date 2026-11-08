package com.cloudmall.order.model.resp;

/**
 * 微信支付回调应答。
 *
 * @param code 应答码
 * @param message 应答信息
 * @author : Tomatos
 */
public record WechatNotifyResp(String code, String message) {

    /**
     * 成功应答。
     *
     * @return 成功应答
     */
    public static WechatNotifyResp success() {
        return new WechatNotifyResp("SUCCESS", "成功");
    }

    /**
     * 失败应答。
     *
     * @param message 失败原因
     * @return 失败应答
     */
    public static WechatNotifyResp fail(String message) {
        return new WechatNotifyResp("FAIL", message);
    }
}
