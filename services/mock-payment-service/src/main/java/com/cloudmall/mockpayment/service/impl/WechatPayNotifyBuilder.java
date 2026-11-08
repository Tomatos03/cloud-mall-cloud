package com.cloudmall.mockpayment.service.impl;

import java.time.LocalDateTime;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.mockpayment.convert.MockPaymentConverter;
import com.cloudmall.mockpayment.model.entity.MockPaymentOrderDO;
import com.cloudmall.mockpayment.properties.MockPaymentProperties;
import com.cloudmall.payment.api.constant.WechatPayConstants;
import com.cloudmall.payment.api.model.bo.WechatPayNotification;
import com.cloudmall.payment.api.model.bo.WechatPayTransaction;
import com.cloudmall.payment.api.util.WechatPayCrypto;

/**
 * 微信支付风格回调报文构造器。
 * <p>
 * 独立成组件便于测试加密报文与业务侧解密逻辑的兼容性。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class WechatPayNotifyBuilder {

    /** 回调最大报文长度（微信支付限制 1MB） */
    private static final int MAX_NOTIFY_BODY_LENGTH = 1024 * 1024;

    private final MockPaymentConverter converter;
    private final MockPaymentProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * 构造支付成功回调报文。
     *
     * @param order 支付单
     * @return 加密后的回调 JSON
     */
    public String build(MockPaymentOrderDO order) {
        WechatPayTransaction transaction = converter.toTransaction(order);
        String plaintext = toJson(transaction);
        String nonce = WechatPayCrypto.randomNonce(6);
        String ciphertext = WechatPayCrypto.aesGcmEncrypt(
                properties.getApiV3Key(),
                WechatPayConstants.ORIGINAL_TYPE_TRANSACTION,
                nonce,
                plaintext
        );
        WechatPayNotification notification = new WechatPayNotification(
                "MOCK-EV-" + IdUtil.fastSimpleUUID(),
                converter.formatTime(LocalDateTime.now().withNano(0)),
                WechatPayConstants.RESOURCE_TYPE_ENCRYPT,
                WechatPayConstants.EVENT_TRANSACTION_SUCCESS,
                "支付成功",
                new WechatPayNotification.Resource(
                        WechatPayConstants.ORIGINAL_TYPE_TRANSACTION,
                        WechatPayConstants.ALGORITHM_AEAD_AES_256_GCM,
                        ciphertext,
                        WechatPayConstants.ORIGINAL_TYPE_TRANSACTION,
                        nonce
                )
        );
        String body = toJson(notification);
        if (body.length() > MAX_NOTIFY_BODY_LENGTH) {
            throw new BizException(BizErrorEnum.PAYMENT_NOTIFY_FAILED);
        }
        return body;
    }

    /**
     * JSON 序列化。
     *
     * @param value 对象
     * @return JSON 字符串
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(BizErrorEnum.PAYMENT_NOTIFY_FAILED);
        }
    }
}
