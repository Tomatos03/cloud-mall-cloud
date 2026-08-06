package com.cloudmall.seckill.job;

import java.time.LocalDateTime;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.seckill.mapper.SeckillActivityMapper;

/**
 * 秒杀活动状态自动流转 JobHandler
 * <p>
 * PUBLISHED → RUNNING（到达 start_time）、RUNNING → FINISHED（到达 end_time）。
 * 均为条件更新，重复执行不会越过状态机。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillActivityStatusJobHandler {

    private final SeckillActivityMapper activityMapper;

    /**
     * 每分钟扫描一次活动状态流转。
     *
     * @param param 任务参数（未使用）
     */
    @XxlJob("seckillActivityStatus")
    public void flowStatus(String param) {
        LocalDateTime now = LocalDateTime.now();
        try {
            int started = activityMapper.markStartedIfPublished(now);
            int finished = activityMapper.markFinishedIfRunning(now);

            if (started > 0 || finished > 0) {
                log.info("秒杀活动状态流转 开始={} 结束={}", started, finished);
            }
        } catch (Exception e) {
            log.error("秒杀活动状态流转失败", e);
        }
    }
}
