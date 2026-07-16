package com.cloudmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.OrderItemDO;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {
}