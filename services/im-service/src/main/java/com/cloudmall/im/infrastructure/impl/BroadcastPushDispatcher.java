package com.cloudmall.im.infrastructure.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.im.api.model.event.ImMessagePushEvent;
import com.cloudmall.im.infrastructure.IImMessageEventPublisher;
import com.cloudmall.im.infrastructure.IPushDispatcher;

/**
 * 广播推送策略：消息发布到公共 topic，所有实例消费。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
@RequiredArgsConstructor
public class BroadcastPushDispatcher implements IPushDispatcher {

    private final IImMessageEventPublisher publisher;

    @Override
    public void dispatch(ImMessagePushEvent event) {
        publisher.publish(event);
    }
}
