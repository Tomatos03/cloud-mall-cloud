package com.cloudmall.user.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudmall.user.service.IStoreOnboardingService;

/**
 * 商家入驻自动审核 JobHandler。
 * <p>
 * 由 XXL-JOB 统一调度，避免多实例部署时每个进程各自用本地定时器重复审核。
 * 仅当配置了正数延迟秒数时执行，生产环境应将该值设为 0，改用人工审核接口。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreApplicationReviewJobHandler {

    private final IStoreOnboardingService storeOnboardingService;

    /** 自动审核延迟秒数，0 表示关闭 */
    @Value("${cloudmall.merchant.auto-review-delay-seconds:0}")
    private long autoReviewDelaySeconds;

    /**
     * 扫描并自动审核到期申请。
     */
    @XxlJob("storeApplicationAutoReview")
    public void autoReview() {
        if (autoReviewDelaySeconds <= 0) {
            return;
        }
        storeOnboardingService.autoApprovePending(autoReviewDelaySeconds);
    }
}
