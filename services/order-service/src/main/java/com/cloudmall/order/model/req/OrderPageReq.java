package com.cloudmall.order.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.order.entity.OrderDO;

/**
 * 订单分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageReq extends PageReq<OrderDO> {

    /** 用户 ID（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private Long userId;

    public OrderPageReq(long current, long size) {
        super(current, size);
    }

    @Override
    public LambdaQueryWrapper<OrderDO> buildLambdaWrapper() {
        LambdaQueryWrapper<OrderDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(OrderDO::getCreateTime);
        return wrapper;
    }
}
