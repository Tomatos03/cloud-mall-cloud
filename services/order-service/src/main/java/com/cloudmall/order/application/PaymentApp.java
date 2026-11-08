package com.cloudmall.order.application;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.cloudmall.common.exception.FeignRemoteException;
import com.cloudmall.order.config.PaymentProperties;
import com.cloudmall.order.convert.PaymentConverter;
import com.cloudmall.order.model.bo.PaymentOrderSnapshot;
import com.cloudmall.order.model.resp.PaymentCreateResp;
import com.cloudmall.order.model.resp.PaymentStatusResp;
import com.cloudmall.order.payment.WechatPayCallbackVerifier;
import com.cloudmall.order.service.IOrderEventService;
import com.cloudmall.order.service.IPaymentService;
import com.cloudmall.payment.api.client.InternalMockPaymentClient;
import com.cloudmall.payment.api.enums.TradeStateEnum;
import com.cloudmall.payment.api.model.bo.WechatPayTransaction;
import com.cloudmall.payment.api.model.req.MockPaymentCreateReq;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;
import com.cloudmall.user.api.client.InternalUserClient;
import com.cloudmall.user.api.model.req.OrderPointAwardReq;

/**
 * 订单支付应用服务。
 * <p>
 * 编排业务订单与第三方支付渠道之间的交互：创建支付单、主动查询支付状态、
 * 以及接收微信支付风格异步回调并驱动订单进入已支付状态。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApp {

    private final IPaymentService paymentService;
    private final IOrderEventService orderEventService;
    private final InternalMockPaymentClient internalMockPaymentClient;
    private final PaymentConverter paymentConverter;
    private final PaymentProperties paymentProperties;
    private final WechatPayCallbackVerifier callbackVerifier;
    private final InternalUserClient internalUserClient;

    /**
     * 创建支付单。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID
     * @return 支付单信息
     */
    public PaymentCreateResp create(String orderNo, Long userId) {
        PaymentOrderSnapshot snapshot = paymentService.getPayableOrder(orderNo, userId);
        Integer amountTotal = snapshot.payableAmount()
                                      .movePointRight(2)
                                      .intValueExact();
        MockPaymentCreateResp resp = internalMockPaymentClient.create(
                MockPaymentCreateReq.builder()
                                    .outTradeNo(orderNo)
                                    .description("CloudMall订单-" + orderNo)
                                    .amountTotal(amountTotal)
                                    .notifyUrl(paymentProperties.getNotifyUrl())
                                    .attach("cloudmall")
                                    .payerOpenid("mock-openid-" + userId)
                                    .tradeType("NATIVE")
                                    .build()
        );
        return paymentConverter.toCreateResp(resp, snapshot.payableAmount());
    }

    /**
     * 主动查询支付状态，并在渠道已支付时补偿订单状态。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID
     * @return 支付状态
     */
    public PaymentStatusResp query(String orderNo, Long userId) {
        PaymentOrderSnapshot snapshot = paymentService.getOrder(orderNo, userId);
        MockPaymentQueryResp resp = internalMockPaymentClient.query(orderNo);
        if (isSuccess(resp.getTradeState()) && !snapshot.isPaid()) {
            reconcile(orderNo, resp.getAmountTotal(), snapshot.userId());
        }
        return paymentConverter.toStatusResp(resp);
    }

    /**
     * 查询订单是否已支付（前端轮询入口）。 <p>
     * 本地已支付直接返回；否则主动查询渠道，查询异常时降级为本地未支付状态， 避免支付渠道短暂不可用导致订
     * 单页无法打开。
     *
     * @param orderNo 父订单号
     * @param userId 当前用户 ID
     * @return true 表示已支付
     */
    public boolean isPaid(String orderNo, Long userId) {
        PaymentOrderSnapshot snapshot = paymentService.getOrder(orderNo, userId);
        if (snapshot.isPaid()) {
            return true;
        }
        try {
            MockPaymentQueryResp resp = internalMockPaymentClient.query(orderNo);
            if (!isSuccess(resp.getTradeState())) {
                return false;
            }
            reconcile(orderNo, resp.getAmountTotal(), snapshot.userId());
            return true;
        } catch (FeignRemoteException | FeignException e) {
            log.warn("查询模拟支付状态失败 orderNo={}", orderNo, e);
            return false;
        }
    }

    /**
     * 处理微信支付异步回调。
     *
     * @param headers HTTP 头
     * @param body 原始回调报文
     */
    public void handleNotify(HttpHeaders headers, String body) {
        WechatPayTransaction tx = callbackVerifier.verifyAndDecrypt(headers, body);
        if (!isSuccess(tx.tradeState())) {
            log.info("忽略非成功支付回调 orderNo={} tradeState={}", tx.outTradeNo(), tx.tradeState());
            return;
        }
        PaymentOrderSnapshot snapshot = paymentService.getOrder(tx.outTradeNo(), null);
        if (snapshot.isPaid()) {
            log.info("订单已支付，幂等跳过回调 orderNo={}", tx.outTradeNo());
            return;
        }
        if (!snapshot.isPendingPayment()) {
            log.warn("订单状态不可支付，忽略回调 orderNo={} status={}", tx.outTradeNo(), snapshot.status());
            return;
        }
        orderEventService.handlePaid(tx.outTradeNo(), paymentService.toYuan(tx.amount().total()));
        awardOrderPoints(tx.outTradeNo(), snapshot.userId(), snapshot.payableAmount());
        log.info("模拟支付回调已受理 orderNo={} transactionId={}", tx.outTradeNo(), tx.transactionId());
    }

    /**
     * 将渠道支付结果补偿到订单状态。
     *
     * @param orderNo 父订单号
     * @param amountTotal 支付金额（分）
     * @param userId 用户 ID
     */
    private void reconcile(String orderNo, Integer amountTotal, Long userId) {
        orderEventService.handlePaid(orderNo, paymentService.toYuan(amountTotal));
        awardOrderPoints(orderNo, userId, paymentService.toYuan(amountTotal));
        log.info("主动查询补偿订单支付状态 orderNo={}", orderNo);
    }

    /**
     * 发放订单积分，失败不影响支付主流程。
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @param amount 实付金额
     */
    private void awardOrderPoints(String orderNo, Long userId, java.math.BigDecimal amount) {
        try {
            internalUserClient.awardOrderPoints(
                    OrderPointAwardReq.builder()
                                      .userId(userId)
                                      .orderNo(orderNo)
                                      .amount(amount)
                                      .build()
            );
        } catch (Exception e) {
            log.warn("订单支付积分发放失败 orderNo={} userId={}", orderNo, userId, e);
        }
    }

    /**
     * 判断渠道交易状态是否成功。
     *
     * @param tradeState 交易状态
     * @return true 表示支付成功
     */
    private static boolean isSuccess(String tradeState) {
        return TradeStateEnum.SUCCESS.getCode().equals(tradeState);
    }
}
