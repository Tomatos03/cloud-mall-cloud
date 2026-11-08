package com.cloudmall.payment.api.constant;

/**
 * 微信支付 API v3 通用常量。
 * <p>
 * 对齐微信支付官方文档中的认证类型、回调通知头、事件类型与加密算法标识，
 * 供模拟支付服务与业务侧回调处理共同引用，避免魔法字符串漂移。
 *
 * @author : Tomatos
 */
public interface WechatPayConstants {

    /** 请求认证类型 */
    String AUTH_SCHEME = "WECHATPAY2-SHA256-RSA2048";

    /** 回调签名头 */
    String HEADER_SIGNATURE = "Wechatpay-Signature";

    /** 回调时间戳头 */
    String HEADER_TIMESTAMP = "Wechatpay-Timestamp";

    /** 回调随机串头 */
    String HEADER_NONCE = "Wechatpay-Nonce";

    /** 回调平台证书序列号头 */
    String HEADER_SERIAL = "Wechatpay-Serial";

    /** 回调签名类型头 */
    String HEADER_SIGNATURE_TYPE = "Wechatpay-Signature-Type";

    /** 支付成功通知事件 */
    String EVENT_TRANSACTION_SUCCESS = "TRANSACTION.SUCCESS";

    /** 退款成功通知事件 */
    String EVENT_REFUND_SUCCESS = "REFUND.SUCCESS";

    /** 加密资源通知类型 */
    String RESOURCE_TYPE_ENCRYPT = "encrypt-resource";

    /** 通知资源原始类型：交易 */
    String ORIGINAL_TYPE_TRANSACTION = "transaction";

    /** 通知资源加密算法 */
    String ALGORITHM_AEAD_AES_256_GCM = "AEAD_AES_256_GCM";

    /** 回调成功应答码 */
    String NOTIFY_SUCCESS_CODE = "SUCCESS";

    /** 回调失败应答码 */
    String NOTIFY_FAIL_CODE = "FAIL";

    /** 交易成功状态 */
    String TRADE_STATE_SUCCESS = "SUCCESS";

    /** 交易未支付状态 */
    String TRADE_STATE_NOTPAY = "NOTPAY";

    /** 交易已关闭状态 */
    String TRADE_STATE_CLOSED = "CLOSED";

    /** 币种：人民币 */
    String CURRENCY_CNY = "CNY";

    /** 签名时间戳允许的最大偏差（秒） */
    long MAX_TIMESTAMP_SKEW_SECONDS = 300L;

    /** RSA 密钥算法 */
    String RSA_ALGORITHM = "RSA";

    /** RSA 签名算法 */
    String RSA_SIGN_ALGORITHM = "SHA256withRSA";

    /** AES 密钥算法 */
    String AES_ALGORITHM = "AES";

    /** AES-GCM 变换 */
    String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";

    /** GCM 认证标签长度（位） */
    int GCM_TAG_LENGTH_BITS = 128;

    /** APIv3 密钥长度（字节） */
    int API_V3_KEY_LENGTH = 32;
}
