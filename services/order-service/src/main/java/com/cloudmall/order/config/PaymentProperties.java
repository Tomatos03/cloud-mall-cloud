package com.cloudmall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 订单支付配置。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "cloudmall.payment")
public class PaymentProperties {

    /** 支付回调地址（模拟支付服务直接访问） */
    private String notifyUrl = "http://localhost:9005/orders/payment/notify/wechat";

    /** 模拟公众号 APPID */
    private String appid = "wxmock000000000001";

    /** 模拟商户号 */
    private String mchid = "1900000001";

    /** APIv3 密钥（32 字节） */
    private String apiV3Key = "cloudmall-mock-payment-apiv3-key";

    /** 模拟支付平台证书序列号 */
    private String platformSerialNo = "MOCKPLATFORM20260101";

    /** 平台公钥资源（回调验签） */
    private Resource platformPublicKey = new ClassPathResource("keys/mock-payment-platform-public.pem");

    /** 是否校验回调签名 */
    private boolean verifySignature = true;
}
