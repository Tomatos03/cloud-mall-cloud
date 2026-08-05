package com.cloudmall.consumer.service.impl;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.cloudmall.consumer.entity.MessageConsumeRecordDO;
import com.cloudmall.consumer.enums.ConsumeStatusEnum;
import com.cloudmall.consumer.mapper.MessageConsumeRecordMapper;
import com.cloudmall.rocketmq.consumer.IMessageConsumeService;

/**
 * 消息消费幂等服务实现
 * <p>
 * consumer 固定为 consumer-service，operation_type 区分注册发券与订单事件。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class MessageConsumeService implements IMessageConsumeService {

    /** 消费者标识 */
    private static final String CONSUMER = "consumer-service";

    private final MessageConsumeRecordMapper messageConsumeRecordMapper;

    /**
     * 判断事件是否已消费。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型
     * @return true 表示已消费
     */
    @Override
    public boolean alreadyConsumed(String eventId, String operationType) {
        Long count = messageConsumeRecordMapper.selectCount(
                Wrappers.<MessageConsumeRecordDO>lambdaQuery()
                        .eq(MessageConsumeRecordDO::getEventId, eventId)
                        .eq(MessageConsumeRecordDO::getConsumer, CONSUMER)
                        .eq(MessageConsumeRecordDO::getOperationType, operationType)
        );
        return count != null && count > 0;
    }

    /**
     * 标记事件消费成功（唯一键冲突忽略，表示并发已消费）。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型
     */
    @Override
    public void markConsumed(String eventId, String operationType) {
        MessageConsumeRecordDO record = MessageConsumeRecordDO.builder()
                                                              .eventId(eventId)
                                                              .consumer(CONSUMER)
                                                              .operationType(operationType)
                                                              .status(ConsumeStatusEnum.CONSUMED)
                                                              .retryCount(0)
                                                              .consumeTime(LocalDateTime.now())
                                                              .build();
        try {
            messageConsumeRecordMapper.insert(record);
        } catch (DuplicateKeyException ignored) {
            // 并发重复消费：幂等跳过
        }
    }
}
