package com.cloudmall.mockpayment.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟支付服务公开配置（便利联调）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockPaymentConfigResp {

    /** 模拟公众号 APPID */
    private String appid;

    /** 模拟商户号 */
    private String mchid;

    /** 对外访问基础地址 */
    private String publicBaseUrl;

    /** 平台证书序列号 */
    private String platformSerialNo;

    /** 平台公钥 PEM（回调验签使用） */
    private String platformPublicKey;

    /** 是否校验商户请求签名 */
    private boolean verifyRequest;
}
