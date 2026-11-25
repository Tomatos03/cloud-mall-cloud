package com.cloudmall.im.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.cloudmall.im.api.model.event.ImMessagePushEvent;
import com.cloudmall.im.convert.ChatMessageConverter;
import com.cloudmall.im.infrastructure.push.IImMessagePushService;
import com.cloudmall.im.infrastructure.store.IDeliveryDedupeStore;

/**
 * IM 消息推送消费者。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${cloudmall.im.mq.topic:im-message-push}",
        consumerGroup = "${cloudmall.im.mq.consumer-group:im-service}",
        selectorType = SelectorType.TAG,
        selectorExpression = "${cloudmall.im.mq.tag:push}",
        messageModel = MessageModel.BROADCASTING,
        maxReconsumeTimes = 16
)
public class ImMessagePushConsumer implements RocketMQListener<ImMessagePushEvent> {

    private final IDeliveryDedupeStore dedupeStore;
    private final ChatMessageConverter converter;
    private final IImMessagePushService pushService;

    @Override
    public void onMessage(ImMessagePushEvent event) {
        String messageId = String.valueOf(event.getMessageId());
        if (!dedupeStore.tryMark(messageId)) {
            return;
        }
        boolean delivered = pushService.pushToLocal(event.getReceiverId(), converter.toResp(event));
        if (!delivered) {
            log.info("消息 {} 未在本实例命中接收方连接，忽略", messageId);
        }
    }
}
