package com.cloudmall.rocketmq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.rocketmq.entity.MessageConsumeRecordDO;

/**
 * 消息消费幂等记录 Mapper
 * <p>
 * 由 MessageConsumeAutoConfiguration 的 @AutoConfigurationPackage + @AutoConfigureBefore
 * 保证被 MyBatis @Mapper 自动扫描注册。
 *
 * @author : Tomatos
 */
@Mapper
public interface MessageConsumeRecordMapper extends BaseMapper<MessageConsumeRecordDO> {

    /**
     * 判断同一事件同一操作是否已消费（唯一键三要素查询）。
     *
     * @param eventId 事件 ID
     * @param consumer 消费者标识
     * @param operationType 操作类型
     * @return true 表示已消费
     */
    default boolean existsConsumed(String eventId, String consumer, String operationType) {
        Long count = selectCount(
                Wrappers.<MessageConsumeRecordDO>lambdaQuery()
                        .eq(MessageConsumeRecordDO::getEventId, eventId)
                        .eq(MessageConsumeRecordDO::getConsumer, consumer)
                        .eq(MessageConsumeRecordDO::getOperationType, operationType)
        );
        return count != null && count > 0;
    }
}
