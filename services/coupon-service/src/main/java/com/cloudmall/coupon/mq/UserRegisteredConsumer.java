package com.cloudmall.coupon.mq;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.cloudmall.auth.api.event.UserRegisteredEvent;
import com.cloudmall.coupon.service.IMessageConsumeService;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 用户注册事件消费者
 * <p>
 * 消费 auth-service 发布的 UserRegistered，幂等发放新人券。
 * 业务幂等（issuedEventId + user_coupon 唯一索引）+ 消费记录兜底，
 * 失败抛异常触发 RocketMQ 重试，超限进入死信队列。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "USER_REGISTERED", consumerGroup = "coupon-user-registered")
public class UserRegisteredConsumer implements RocketMQListener<UserRegisteredEvent> {

    private final IMessageConsumeService consumeService;
    private final IUserCouponService userCouponService;

    /**
     * 处理注册事件：已消费跳过，业务失败抛异常重试。
     *
     * @param event 注册事件
     */
    @Override
    public void onMessage(UserRegisteredEvent event) {
        if (event == null || StrUtil.isBlank(event.getEventId())) {
            return;
        }
        if (consumeService.alreadyConsumed(event.getEventId(), IMessageConsumeService.OP_REGISTER_AUTO_ISSUE)) {
            return;
        }
        try {
            userCouponService.autoIssueForRegister(event.getUserId(), event.getEventId());
            consumeService.markConsumed(event.getEventId(), IMessageConsumeService.OP_REGISTER_AUTO_ISSUE);
        } catch (RuntimeException e) {
            log.error("注册自动发券失败 userId={} eventId={}", event.getUserId(), event.getEventId(), e);
            throw e;
        }
    }
}
