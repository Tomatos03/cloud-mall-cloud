package com.cloudmall.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.entity.MessageConsumeRecordDO;

/**
 * 消息消费幂等记录 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface MessageConsumeRecordMapper extends BaseMapper<MessageConsumeRecordDO> {
}
