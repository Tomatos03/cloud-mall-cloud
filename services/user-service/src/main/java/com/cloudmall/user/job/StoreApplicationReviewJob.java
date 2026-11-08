package com.cloudmall.user.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloudmall.user.service.IStoreOnboardingService;

/**
 * 商家入驻自动审核任务。
 * <p>
 * 仅当配置了正数延迟秒数时启用，用于本地演示与联调；
 * 生产环境应将 cloudmall.merchant.auto-review-delay-seconds 设为 0，走人工审核接口。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreApplicationReviewJob {

    private final IStoreOnboardingService storeOnboardingService;

    /** 自动审核延迟秒数，0 表示关闭 */
    @Value("${cloudmall.merchant.auto-review-delay-seconds:0}")
    private long autoReviewDelaySeconds;

    /**
     * 扫描并自动审核到期申请。
     */
    @Scheduled(
            fixedDelayString = "${cloudmall.merchant.review-scan-interval-ms:5000}",
            initialDelayString = "${cloudmall.merchant.review-scan-interval-ms:5000}"
    )
    public void autoReview() {
        if (autoReviewDelaySeconds <= 0) {
            return;
        }
        try {
            storeOnboardingService.autoApprovePending(autoReviewDelaySeconds);
        } catch (RuntimeException e) {
            log.warn("商家入驻自动审核任务执行失败", e);
        }
    }
}
