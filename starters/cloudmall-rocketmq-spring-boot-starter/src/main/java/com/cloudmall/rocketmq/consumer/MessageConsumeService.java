package com.cloudmall.rocketmq.consumer;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;

import com.cloudmall.rocketmq.entity.MessageConsumeRecordDO;
import com.cloudmall.rocketmq.enums.ConsumeStatusEnum;
import com.cloudmall.rocketmq.mapper.MessageConsumeRecordMapper;

/**
 * 消息消费幂等服务实现（通用）
 * <p>
 * consumer 标识由 MqAutoConfiguration 注入（默认取
 * spring.application.name，可用 cloudmall.rocketmq.consumer 覆盖），
 * operation_type 词汇由消费方定义。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class MessageConsumeService implements IMessageConsumeService {

    private final MessageConsumeRecordMapper messageConsumeRecordMapper;
    private final String consumer;

    /**
     * 判断事件是否已消费。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型
     * @return true 表示已消费
     */
    @Override
    public boolean alreadyConsumed(String eventId, String operationType) {
        return messageConsumeRecordMapper.existsConsumed(eventId, consumer, operationType);
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
                                                              .consumer(consumer)
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
