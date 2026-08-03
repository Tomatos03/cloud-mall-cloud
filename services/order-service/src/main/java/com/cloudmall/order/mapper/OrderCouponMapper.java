package com.cloudmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.order.entity.OrderCouponDO;

/**
 * 订单优惠关联 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderCouponMapper extends BaseMapper<OrderCouponDO> {
}
