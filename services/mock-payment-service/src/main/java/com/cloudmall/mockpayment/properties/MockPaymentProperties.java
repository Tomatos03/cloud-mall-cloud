package com.cloudmall.mockpayment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * 模拟支付服务配置。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "mock-payment")
public class MockPaymentProperties {

    /** 模拟公众号 APPID */
    private String appid;

    /** 模拟商户号 */
    private String mchid;

    /** 对外访问基础地址（用于拼接收银台地址） */
    private String publicBaseUrl;

    /** APIv3 密钥（32 字节） */
    private String apiV3Key;

    /** 模拟商户证书序列号 */
    private String merchantSerialNo;

    /** 模拟平台证书序列号 */
    private String platformSerialNo;

    /** 商户公钥（校验商户请求签名） */
    private Resource merchantPublicKey;

    /** 平台私钥（对回调报文签名） */
    private Resource platformPrivateKey;

    /** 平台公钥（回调方验签） */
    private Resource platformPublicKey;

    /** 签名相关配置 */
    private Signature signature = new Signature();

    /** 回调通知相关配置 */
    private Notify notify = new Notify();

    /** 默认支付有效期（分钟） */
    private int defaultExpireMinutes = 15;

    /**
     * 请求签名配置。
     */
    @Data
    public static class Signature {

        /** 是否校验商户请求签名 */
        private boolean verifyRequest;
    }

    /**
     * 回调通知配置。
     */
    @Data
    public static class Notify {

        /** 最大重试次数 */
        private int maxRetry = 5;

        /** 重试扫描间隔（毫秒） */
        private long retryIntervalMs = 30000L;

        /** 连接超时（毫秒） */
        private int connectTimeoutMs = 2000;

        /** 读取超时（毫秒） */
        private int readTimeoutMs = 5000;
    }
}
