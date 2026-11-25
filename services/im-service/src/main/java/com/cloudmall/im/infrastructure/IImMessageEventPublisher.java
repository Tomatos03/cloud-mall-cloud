package com.cloudmall.im.infrastructure;

import com.cloudmall.im.api.model.event.ImMessagePushEvent;

/**
 * IM 消息 MQ 发布器。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IImMessageEventPublisher {

    void publish(ImMessagePushEvent event);
}
