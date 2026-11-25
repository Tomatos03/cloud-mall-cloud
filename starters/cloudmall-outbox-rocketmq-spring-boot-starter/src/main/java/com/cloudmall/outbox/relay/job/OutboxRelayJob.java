package com.cloudmall.outbox.relay.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;

import com.cloudmall.outbox.relay.service.OutboxRelay;

/**
 * Outbox Relay 定时任务。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@RequiredArgsConstructor
public class OutboxRelayJob {

    private final OutboxRelay outboxRelay;

    @XxlJob("outboxRelay")
    public void relay() {
        outboxRelay.relay();
    }
}
