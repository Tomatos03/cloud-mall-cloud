package com.cloudmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.OrderSubDO;

/**
 * 店铺子订单 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderSubMapper extends BaseMapper<OrderSubDO> {
}
