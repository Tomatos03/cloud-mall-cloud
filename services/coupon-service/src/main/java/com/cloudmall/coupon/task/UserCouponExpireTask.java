package com.cloudmall.coupon.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 用户券过期批处理任务
 * <p>
 * 将已过期的 AVAILABLE 用户券批量落库为 EXPIRED 并追加状态流水。
 * LOCKED 状态由订单释放流程处理，本任务不直接改写。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCouponExpireTask {

    private final IUserCouponService userCouponService;

    /**
     * 每 5 分钟执行一次批量过期。
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void expireOverdue() {
        try {
            int affected = userCouponService.expireOverdue();
            if (affected > 0) {
                log.info("批量过期用户券数量: {}", affected);
            }
        } catch (Exception e) {
            log.error("批量过期用户券失败", e);
        }
    }
}
