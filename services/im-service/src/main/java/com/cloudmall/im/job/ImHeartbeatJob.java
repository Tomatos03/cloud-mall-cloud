package com.cloudmall.im.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.im.infrastructure.IConnectionHeartbeatManager;

/**
 * IM 连接心跳定时任务。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
@RequiredArgsConstructor
public class ImHeartbeatJob {

    private final IConnectionHeartbeatManager heartbeatManager;

    @XxlJob("imHeartbeatPing")
    public void ping() {
        heartbeatManager.ping();
    }

    @XxlJob("imStaleSessionCleanup")
    public void cleanupStale() {
        heartbeatManager.cleanupStale();
    }
}
