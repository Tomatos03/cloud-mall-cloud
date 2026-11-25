package com.cloudmall.im.infrastructure.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.im.api.model.event.ImMessagePushEvent;
import com.cloudmall.im.infrastructure.IImMessageEventPublisher;
import com.cloudmall.im.infrastructure.ImProperties;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * IM 消息 RocketMQ 发布器实现。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
@RequiredArgsConstructor
public class ImMessageEventPublisher implements IImMessageEventPublisher {

    private final MQProducer mqProducer;
    private final ImProperties properties;

    @Override
    public void publish(ImMessagePushEvent event) {
        String destination = destination();
        String keys = String.valueOf(event.getMessageId());
        mqProducer.sendSyncOrderly(destination, event, String.valueOf(event.getSessionId()), keys);
    }

    private String destination() {
        ImProperties.Mq mq = properties.getMq();
        return StrUtil.isBlank(mq.getTag()) ? mq.getTopic() : mq.getTopic() + ":" + mq.getTag();
    }
}
