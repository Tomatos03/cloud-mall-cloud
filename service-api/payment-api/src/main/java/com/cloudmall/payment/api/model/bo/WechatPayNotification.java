package com.cloudmall.payment.api.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 微信支付回调通知报文。
 * <p>
 * 对应微信支付 POST 到商户 notify_url 的原始 JSON 结构，
 * resource.ciphertext 需使用 APIv3 密钥进行 AES-256-GCM 解密。
 *
 * @param id 通知 ID
 * @param createTime 通知创建时间（RFC3339）
 * @param resourceType 通知数据类型
 * @param eventType 通知类型
 * @param summary 回调摘要
 * @param resource 加密资源
 * @author : Tomatos
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WechatPayNotification(
    String id,
    String createTime,
    String resourceType,
    String eventType,
    String summary,
    Resource resource
) {

    /**
     * 加密资源。
     *
     * @param originalType 原始类型
     * @param algorithm 加密算法
     * @param ciphertext 数据密文
     * @param associatedData 附加数据
     * @param nonce 随机串
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Resource(
        String originalType,
        String algorithm,
        String ciphertext,
        String associatedData,
        String nonce
    ) {
    }
}
