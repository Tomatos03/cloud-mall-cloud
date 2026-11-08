package com.cloudmall.order.payment;

import java.security.PublicKey;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.config.PaymentProperties;
import com.cloudmall.payment.api.constant.WechatPayConstants;
import com.cloudmall.payment.api.model.bo.WechatPayNotification;
import com.cloudmall.payment.api.model.bo.WechatPayTransaction;
import com.cloudmall.payment.api.util.WechatPayCrypto;

/**
 * 微信支付回调验签与解密组件。
 * <p>
 * 按微信支付 API v3 规范构造应答签名串，使用平台公钥验签，
 * 再使用 APIv3 密钥对 resource.ciphertext 做 AES-256-GCM 解密。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatPayCallbackVerifier {

    private final PaymentProperties properties;
    private final ObjectMapper objectMapper;

    private PublicKey platformPublicKey;

    /**
     * 初始化平台公钥。
     */
    @PostConstruct
    public void init() {
        platformPublicKey = WechatPayCrypto.loadPublicKey(properties.getPlatformPublicKey());
    }

    /**
     * 校验回调签名并解密交易报文。
     *
     * @param headers HTTP 头
     * @param body 原始回调报文
     * @return 解密后的交易报文
     */
    public WechatPayTransaction verifyAndDecrypt(HttpHeaders headers, String body) {
        Asserts.notBlank(body, BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        if (properties.isVerifySignature()) {
            verifySignature(headers, body);
        }
        WechatPayNotification notification = parse(body, WechatPayNotification.class);
        Asserts.isEqual(
                notification.resourceType(),
                WechatPayConstants.RESOURCE_TYPE_ENCRYPT,
                BizErrorEnum.PAYMENT_CALLBACK_INVALID
        );
        Asserts.isEqual(
                notification.eventType(),
                WechatPayConstants.EVENT_TRANSACTION_SUCCESS,
                BizErrorEnum.PAYMENT_CALLBACK_INVALID
        );
        WechatPayNotification.Resource resource = Asserts.requireNonNull(
                notification.resource(),
                BizErrorEnum.PAYMENT_CALLBACK_INVALID
        );
        Asserts.isEqual(
                resource.algorithm(),
                WechatPayConstants.ALGORITHM_AEAD_AES_256_GCM,
                BizErrorEnum.PAYMENT_CALLBACK_INVALID
        );
        Asserts.isEqual(
                resource.originalType(),
                WechatPayConstants.ORIGINAL_TYPE_TRANSACTION,
                BizErrorEnum.PAYMENT_CALLBACK_INVALID
        );
        String plaintext = decrypt(resource);
        WechatPayTransaction transaction = parse(plaintext, WechatPayTransaction.class);
        Asserts.notBlank(transaction.outTradeNo(), BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        Asserts.notNull(transaction.amount(), BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        Asserts.notNull(transaction.amount().total(), BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        Asserts.isEqual(transaction.mchid(), properties.getMchid(), BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        Asserts.isEqual(transaction.appid(), properties.getAppid(), BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        return transaction;
    }

    /**
     * 校验回调签名。
     *
     * @param headers HTTP 头
     * @param body 原始回调报文
     */
    private void verifySignature(HttpHeaders headers, String body) {
        String signature = headers.getFirst(WechatPayConstants.HEADER_SIGNATURE);
        String timestamp = headers.getFirst(WechatPayConstants.HEADER_TIMESTAMP);
        String nonce = headers.getFirst(WechatPayConstants.HEADER_NONCE);
        String serial = headers.getFirst(WechatPayConstants.HEADER_SERIAL);
        Asserts.notBlank(signature, BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID);
        Asserts.notBlank(timestamp, BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID);
        Asserts.notBlank(nonce, BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID);
        Asserts.notBlank(serial, BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID);
        Asserts.isEqual(
                serial,
                properties.getPlatformSerialNo(),
                BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID
        );
        Asserts.isTrue(timestamp.matches("\\d+"), BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID);
        long now = Instant.now().getEpochSecond();
        Asserts.isTrue(
                Math.abs(now - Long.parseLong(timestamp)) <= WechatPayConstants.MAX_TIMESTAMP_SKEW_SECONDS,
                BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID
        );
        String message = WechatPayCrypto.responseMessage(timestamp, nonce, body);
        Asserts.isTrue(
                WechatPayCrypto.verify(platformPublicKey, message, signature),
                BizErrorEnum.PAYMENT_CALLBACK_SIGNATURE_INVALID
        );
    }

    /**
     * 解密回调资源。
     *
     * @param resource 加密资源
     * @return 明文 JSON
     */
    private String decrypt(WechatPayNotification.Resource resource) {
        try {
            return WechatPayCrypto.aesGcmDecrypt(
                    properties.getApiV3Key(),
                    resource.associatedData(),
                    resource.nonce(),
                    resource.ciphertext()
            );
        } catch (IllegalArgumentException e) {
            log.warn("支付回调报文解密失败", e);
            throw new BizException(BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        }
    }

    /**
     * 解析 JSON。
     *
     * @param json JSON 字符串
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 解析结果
     */
    private <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.warn("支付回调报文格式错误", e);
            throw new BizException(BizErrorEnum.PAYMENT_CALLBACK_INVALID);
        }
    }
}
