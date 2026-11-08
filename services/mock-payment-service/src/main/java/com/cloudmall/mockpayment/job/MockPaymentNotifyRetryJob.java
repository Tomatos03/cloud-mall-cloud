package com.cloudmall.mockpayment.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloudmall.mockpayment.service.IPaymentNotifyService;

/**
 * 模拟支付回调重试任务。
 * <p>
 * 扫描到期未成功的通知记录，按指数退避重新投递；达到最大重试次数后保留失败记录供人工排查。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockPaymentNotifyRetryJob {

    private final IPaymentNotifyService paymentNotifyService;

    /**
     * 扫描并重试到期回调。
     */
    @Scheduled(
            fixedDelayString = "${mock-payment.notify.retry-interval-ms:30000}",
            initialDelayString = "${mock-payment.notify.retry-interval-ms:30000}"
    )
    public void retry() {
        try {
            paymentNotifyService.retryDue();
        } catch (RuntimeException e) {
            log.warn("模拟支付回调重试任务执行失败", e);
        }
    }
}
