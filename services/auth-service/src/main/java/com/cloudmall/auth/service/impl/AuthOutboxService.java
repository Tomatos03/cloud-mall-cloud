package com.cloudmall.auth.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.auth.api.event.UserRegisteredEvent;
import com.cloudmall.auth.entity.OutboxDO;
import com.cloudmall.auth.enums.OutboxPublishStatusEnum;
import com.cloudmall.auth.mapper.OutboxMapper;
import com.cloudmall.auth.service.IAuthOutboxService;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 认证服务 Outbox 服务实现
 * <p>
 * 注册事件与业务状态在同一本地事务提交，定时任务扫描待发布事件
 * 发送 RocketMQ 并更新状态；发送失败按退避重试，超限进入死信。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthOutboxService implements IAuthOutboxService {

    /** 用户注册事件 Topic */
    public static final String USER_REGISTERED_TOPIC = "USER_REGISTERED";

    /** 最大重试次数，超过进入死信 */
    private static final int MAX_RETRY = 10;

    /** 初始重试间隔秒数（指数退避） */
    private static final int RETRY_BASE_SECONDS = 30;

    private final OutboxMapper outboxMapper;
    private final MQProducer mqProducer;

    /**
     * 追加用户注册事件（与注册事务同提交）。
     *
     * @param userId 用户 ID
     * @param username 用户名
     */
    @Override
    public void appendUserRegistered(Long userId, String username) {
        String eventId = UUID.randomUUID()
                             .toString()
                             .replace("-", "");
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                                                       .eventId(eventId)
                                                       .userId(userId)
                                                       .username(username)
                                                       .build();
        OutboxDO outbox = OutboxDO.builder()
                                  .eventId(eventId)
                                  .aggregateType("USER")
                                  .aggregateId(String.valueOf(userId))
                                  .eventType("UserRegistered")
                                  .payload(JSONUtil.toJsonStr(event))
                                  .publishStatus(OutboxPublishStatusEnum.PENDING)
                                  .retryCount(0)
                                  .build();
        outboxMapper.insert(outbox);
    }

    /**
     * 发布一批待发布事件，更新发布状态。
     *
     * @param batchSize 批次大小
     * @return 本次尝试发布条数
     */
    @Override
    public int publishPending(int batchSize) {
        List<OutboxDO> pending = listDuePending(batchSize);
        if (CollUtil.isEmpty(pending)) {
            return 0;
        }
        for (OutboxDO outbox : pending) {
            publishOne(outbox);
        }
        return pending.size();
    }

    /**
     * 查询到期待发布事件（已到重试时间，按创建时间升序）。
     *
     * @param batchSize 批次大小
     * @return 待发布事件列表
     */
    private List<OutboxDO> listDuePending(int batchSize) {
        Page<OutboxDO> page = outboxMapper.selectPage(
                new Page<>(1, batchSize),
                Wrappers.<OutboxDO>lambdaQuery()
                        .eq(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.PENDING)
                        .and(
                                wrapper -> wrapper.isNull(OutboxDO::getNextRetryTime)
                                                  .or()
                                                  .le(OutboxDO::getNextRetryTime, LocalDateTime.now())
                        )
                        .orderByAsc(OutboxDO::getId)
        );
        return page.getRecords();
    }

    /**
     * 发布单条事件：发送成功标记已发布，失败记录重试与死信。
     *
     * @param outbox 事件
     */
    private void publishOne(OutboxDO outbox) {
        try {
            UserRegisteredEvent event = JSONUtil.toBean(
                    JSONUtil.parseObj(outbox.getPayload()),
                    UserRegisteredEvent.class
            );
            mqProducer.sendSync(USER_REGISTERED_TOPIC, event);
            markPublished(outbox);
        } catch (Exception e) {
            log.warn("Outbox 发布失败 eventId={}", outbox.getEventId(), e);
            markFailed(outbox, e);
        }
    }

    /**
     * 标记事件已发布。
     *
     * @param outbox 事件
     */
    private void markPublished(OutboxDO outbox) {
        outboxMapper.update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getPublishStatus, OutboxPublishStatusEnum.PUBLISHED)
                        .set(OutboxDO::getPublishTime, LocalDateTime.now())
                        .set(OutboxDO::getLastError, null)
                        .eq(OutboxDO::getId, outbox.getId())
        );
    }

    /**
     * 记录发布失败：重试次数递增，退避下次重试，超限进入死信。
     *
     * @param outbox 事件
     * @param e 异常
     */
    private void markFailed(OutboxDO outbox, Exception e) {
        int retryCount = outbox.getRetryCount() == null ? 0 : outbox.getRetryCount();
        int nextRetry = retryCount + 1;
        OutboxPublishStatusEnum status = nextRetry >= MAX_RETRY
                ? OutboxPublishStatusEnum.DEAD
                : OutboxPublishStatusEnum.PENDING;
        outboxMapper.update(
                Wrappers.<OutboxDO>lambdaUpdate()
                        .set(OutboxDO::getRetryCount, nextRetry)
                        .set(
                                OutboxDO::getNextRetryTime,
                                LocalDateTime.now().plusSeconds(RETRY_BASE_SECONDS * (long) nextRetry)
                        )
                        .set(OutboxDO::getLastError, e.getMessage())
                        .set(OutboxDO::getPublishStatus, status)
                        .eq(OutboxDO::getId, outbox.getId())
        );
    }
}
