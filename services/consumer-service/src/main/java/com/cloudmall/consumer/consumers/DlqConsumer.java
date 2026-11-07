package com.cloudmall.consumer.consumers;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 死信队列消费者
 * <p>
 * 通配订阅所有消费组的死信队列（{@code %DLQ%*}），承接业务消费重试
 * （{@code maxReconsumeTimes}）超限后转入的消息。收到死信记录完整信息
 * 并告警，不抛异常（ack），避免死信反复重投。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "%DLQ%*",
        consumerGroup = "dlq-handler",
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class DlqConsumer implements RocketMQListener<MessageExt> {

    /**
     * 处理死信消息：记录完整信息供人工排查。
     *
     * @param message 死信消息
     */
    @Override
    public void onMessage(MessageExt message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.error(
                "死信消息 msgId={} topic={} tags={} reconsumeTimes={} body={}",
                message.getMsgId(),
                message.getTopic(),
                message.getTags(),
                message.getReconsumeTimes(),
                body
        );
    }
}
