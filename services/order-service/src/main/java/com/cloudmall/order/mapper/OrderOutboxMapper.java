package com.cloudmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.OrderOutboxDO;

/**
 * 订单服务本地 Outbox Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderOutboxMapper extends BaseMapper<OrderOutboxDO> {
}
