package com.cloudmall.mockpayment.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.mockpayment.properties.MockPaymentProperties;
import com.cloudmall.payment.api.constant.WechatPayConstants;
import com.cloudmall.payment.api.util.WechatPayAuthorization;
import com.cloudmall.payment.api.util.WechatPayCrypto;

/**
 * 微信支付 API v3 签名服务。
 * <p>
 * 负责校验商户请求签名以及生成平台回调签名；商户公钥与平台私钥均通过配置加载。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class WechatPaySignatureService {

    private final MockPaymentProperties properties;

    /**
     * 校验商户请求签名。
     *
     * @param authorization Authorization 请求头
     * @param method HTTP 方法
     * @param requestUri 请求路径
     * @param queryString 查询字符串，可为空
     * @param body 请求体
     */
    public void verifyRequest(
        String authorization,
        String method,
        String requestUri,
        String queryString,
        String body
    ) {
        Asserts.notBlank(authorization, BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID);
        WechatPayAuthorization parsed = WechatPayAuthorization.parse(authorization);
        Asserts.isEqual(parsed.mchid(), properties.getMchid(), BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID);
        Asserts.isEqual(
                parsed.serialNo(),
                properties.getMerchantSerialNo(),
                BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID
        );
        Asserts.notBlank(parsed.timestamp(), BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID);
        Asserts.notBlank(parsed.nonceStr(), BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID);
        Asserts.notBlank(parsed.signature(), BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID);
        Asserts.isTrue(parsed.timestamp().matches("\\d+"), BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID);
        long timestamp = Long.parseLong(parsed.timestamp());
        long now = Instant.now().getEpochSecond();
        Asserts.isTrue(
                Math.abs(now - timestamp) <= WechatPayConstants.MAX_TIMESTAMP_SKEW_SECONDS,
                BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID
        );
        String url = requestUri;
        if (queryString != null && !queryString.isBlank()) {
            url = url + "?" + queryString;
        }
        String message = WechatPayCrypto.requestMessage(
                method,
                url,
                parsed.timestamp(),
                parsed.nonceStr(),
                body == null ? "" : body
        );
        PublicKey merchantPublicKey = WechatPayCrypto.loadPublicKey(properties.getMerchantPublicKey());
        Asserts.isTrue(
                WechatPayCrypto.verify(merchantPublicKey, message, parsed.signature()),
                BizErrorEnum.MOCK_PAYMENT_SIGNATURE_INVALID
        );
    }

    /**
     * 生成平台回调签名。
     *
     * @param body 回调报文
     * @return 回调签名信息
     */
    public SignedCallback signCallback(String body) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = WechatPayCrypto.randomNonce(16);
        String message = WechatPayCrypto.responseMessage(timestamp, nonce, body);
        PrivateKey platformPrivateKey = WechatPayCrypto.loadPrivateKey(properties.getPlatformPrivateKey());
        String signature = WechatPayCrypto.sign(platformPrivateKey, message);
        return new SignedCallback(signature, timestamp, nonce, properties.getPlatformSerialNo());
    }

    /**
     * 获取平台公钥 PEM 文本。
     *
     * @return 平台公钥 PEM
     */
    public String platformPublicKeyPem() {
        Resource resource = properties.getPlatformPublicKey();
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取模拟平台公钥失败", e);
        }
    }

    /**
     * 回调签名信息。
     *
     * @param signature 签名值
     * @param timestamp 时间戳
     * @param nonce 随机串
     * @param serial 平台证书序列号
     */
    public record SignedCallback(String signature, String timestamp, String nonce, String serial) {
    }
}
