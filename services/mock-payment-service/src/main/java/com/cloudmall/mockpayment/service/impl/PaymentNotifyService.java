package com.cloudmall.mockpayment.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.mockpayment.convert.MockPaymentConverter;
import com.cloudmall.mockpayment.enums.NotifyStatusEnum;
import com.cloudmall.mockpayment.mapper.MockPaymentNotifyMapper;
import com.cloudmall.mockpayment.mapper.MockPaymentOrderMapper;
import com.cloudmall.mockpayment.model.entity.MockPaymentNotifyDO;
import com.cloudmall.mockpayment.model.entity.MockPaymentOrderDO;
import com.cloudmall.mockpayment.model.resp.MockPaymentNotifyResp;
import com.cloudmall.mockpayment.properties.MockPaymentProperties;
import com.cloudmall.mockpayment.service.IPaymentNotifyService;
import com.cloudmall.payment.api.constant.WechatPayConstants;

/**
 * 模拟支付回调通知服务实现。
 * <p>
 * 回调报文完全按微信支付 API v3 规范构造：resource.ciphertext 为
 * AES-256-GCM 加密后的交易 JSON，HTTP 头携带平台证书序列号、时间戳、
 * 随机串与 RSA-SHA256 签名；业务侧返回 2xx 且 code=SUCCESS（或空报文）视为投递成功。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotifyService implements IPaymentNotifyService {

    /** 重试扫描批量大小 */
    private static final int RETRY_BATCH_SIZE = 20;

    /** 失败原因最大长度 */
    private static final int MAX_ERROR_LENGTH = 500;

    private final MockPaymentNotifyMapper notifyMapper;
    private final MockPaymentOrderMapper orderMapper;
    private final MockPaymentConverter converter;
    private final WechatPayNotifyBuilder notifyBuilder;
    private final MockPaymentProperties properties;
    private final WechatPaySignatureService signatureService;
    private final RestClient mockPaymentRestClient;
    private final ObjectMapper objectMapper;

    /**
     * 支付成功后创建并投递回调通知（同一订单只保留一条有效回调记录）。
     *
     * @param outTradeNo 商户订单号
     */
    @Override
    public void notifyPaymentSuccess(String outTradeNo) {
        List<MockPaymentNotifyDO> existing = notifyMapper.selectByOutTradeNo(outTradeNo);
        if (!existing.isEmpty()) {
            send(existing.get(0).getId());
            return;
        }
        MockPaymentOrderDO order = Asserts.requireNonNull(
                orderMapper.selectByOutTradeNo(outTradeNo),
                BizErrorEnum.MOCK_PAYMENT_ORDER_NOT_EXIST
        );
        String body = notifyBuilder.build(order);
        LocalDateTime now = LocalDateTime.now();
        MockPaymentNotifyDO notify = MockPaymentNotifyDO
                .builder()
                .outTradeNo(order.getOutTradeNo())
                .eventType(WechatPayConstants.EVENT_TRANSACTION_SUCCESS)
                .notifyUrl(order.getNotifyUrl())
                .requestBody(body)
                .status(NotifyStatusEnum.PENDING.getCode())
                .retryCount(0)
                .nextRetryTime(now)
                .build();
        notifyMapper.insert(notify);
        send(notify.getId());
    }

    /**
     * 投递指定通知记录。
     *
     * @param notifyId 通知记录 ID
     */
    @Override
    public void send(Long notifyId) {
        MockPaymentNotifyDO notify = notifyMapper.selectById(notifyId);
        if (notify == null || NotifyStatusEnum.SUCCESS.getCode().equals(notify.getStatus())) {
            return;
        }
        WechatPaySignatureService.SignedCallback signed = signatureService.signCallback(
                notify.getRequestBody()
        );
        notifyMapper.updateSignature(notifyId, signed.signature());
        NotifyResult response = post(notify, signed);
        if (isSuccessResponse(response)) {
            notifyMapper.markSuccess(notifyId, response.body());
            log.info("模拟支付回调成功 outTradeNo={} notifyId={}", notify.getOutTradeNo(), notifyId);
            return;
        }
        handleFailure(notify, response);
    }

    /**
     * 手动重发指定订单的最近一次回调。
     *
     * @param outTradeNo 商户订单号
     */
    @Override
    public void resend(String outTradeNo) {
        List<MockPaymentNotifyDO> notifies = notifyMapper.selectByOutTradeNo(outTradeNo);
        if (notifies.isEmpty()) {
            throw new BizException(BizErrorEnum.MOCK_PAYMENT_ORDER_NOT_EXIST);
        }
        Long notifyId = notifies.get(0).getId();
        notifyMapper.resetForResend(notifyId, LocalDateTime.now());
        send(notifyId);
    }

    /**
     * 查询订单下的回调记录。
     *
     * @param outTradeNo 商户订单号
     * @return 回调记录
     */
    @Override
    public List<MockPaymentNotifyResp> listByOutTradeNo(String outTradeNo) {
        return converter.toNotifyRespList(notifyMapper.selectByOutTradeNo(outTradeNo));
    }

    /**
     * 重试到期的失败通知。
     */
    @Override
    public void retryDue() {
        List<MockPaymentNotifyDO> candidates = notifyMapper.selectRetryCandidates(
                LocalDateTime.now(),
                properties.getNotify().getMaxRetry(),
                RETRY_BATCH_SIZE
        );
        for (MockPaymentNotifyDO candidate : candidates) {
            try {
                send(candidate.getId());
            } catch (RuntimeException e) {
                log.warn("模拟支付回调重试异常 notifyId={}", candidate.getId(), e);
            }
        }
    }

    /**
     * 投递回调请求。
     *
     * @param notify 通知记录
     * @param signed 签名信息
     * @return 回调应答
     */
    private NotifyResult post(MockPaymentNotifyDO notify, WechatPaySignatureService.SignedCallback signed) {
        try {
            return mockPaymentRestClient
                    .post()
                    .uri(notify.getNotifyUrl())
                    .headers(
                            headers -> {
                                headers.setContentType(MediaType.APPLICATION_JSON);
                                headers.set(WechatPayConstants.HEADER_SIGNATURE, signed.signature());
                                headers.set(WechatPayConstants.HEADER_TIMESTAMP, signed.timestamp());
                                headers.set(WechatPayConstants.HEADER_NONCE, signed.nonce());
                                headers.set(WechatPayConstants.HEADER_SERIAL, signed.serial());
                                headers.set(
                                        WechatPayConstants.HEADER_SIGNATURE_TYPE,
                                        WechatPayConstants.AUTH_SCHEME
                                );
                            }
                    )
                    .body(notify.getRequestBody())
                    .exchange(
                            (request, response) -> {
                                String body = readResponseBody(response.getBody());
                                return new NotifyResult(response.getStatusCode().value(), body);
                            }
                    );
        } catch (RestClientException e) {
            log.warn("模拟支付回调请求失败 outTradeNo={}", notify.getOutTradeNo(), e);
            return new NotifyResult(0, e.getMessage());
        }
    }

    /**
     * 读取应答报文。
     *
     * @param inputStream 应答输入流
     * @return 应答文本
     * @throws IOException 读取失败
     */
    private static String readResponseBody(java.io.InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 判断回调是否被业务侧成功接收。
     *
     * @param response 回调应答
     * @return true 表示接收成功
     */
    private boolean isSuccessResponse(NotifyResult response) {
        boolean successStatus = response.status() >= 200 && response.status() < 300;
        if (!successStatus) {
            return false;
        }
        if (StrUtil.isBlank(response.body())) {
            return true;
        }
        try {
            JsonNode code = objectMapper.readTree(response.body()).path("code");
            return code.isMissingNode() || WechatPayConstants.NOTIFY_SUCCESS_CODE.equals(code.asText());
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 记录失败并计算下一次重试时间。
     *
     * @param notify 通知记录
     * @param response 回调应答
     */
    private void handleFailure(MockPaymentNotifyDO notify, NotifyResult response) {
        int retryCount = ObjectUtil.defaultIfNull(notify.getRetryCount(), 0) + 1;
        long delayMs = retryDelayMs(retryCount);
        LocalDateTime nextRetryTime = LocalDateTime.now().plusNanos(delayMs * 1_000_000L);
        String error = StrUtil.subPre(StrUtil.blankToDefault(response.body(), "回调投递失败"), MAX_ERROR_LENGTH);
        notifyMapper.markFailed(notify.getId(), retryCount, nextRetryTime, response.body(), error);
        log.warn(
                "模拟支付回调失败 outTradeNo={} status={} retryCount={}",
                notify.getOutTradeNo(),
                response.status(),
                retryCount
        );
    }

    /**
     * 计算指数退避重试间隔。
     *
     * @param retryCount 已重试次数
     * @return 间隔毫秒数
     */
    private long retryDelayMs(int retryCount) {
        long base = Math.max(properties.getNotify().getRetryIntervalMs(), 1000L);
        long multiplier = 1L << Math.min(retryCount - 1, 5);
        return Math.min(base * multiplier, 3_600_000L);
    }

    /**
     * 回调应答。
     *
     * @param status HTTP 状态码
     * @param body 应答报文
     */
    private record NotifyResult(int status, String body) {
    }
}
