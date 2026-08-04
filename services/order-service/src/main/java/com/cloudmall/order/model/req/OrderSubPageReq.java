package com.cloudmall.order.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.order.entity.OrderSubDO;

/**
 * 子订单分页查询请求。
 * <p>
 * 商家端强制注入 shopId，运营端可按 shopId 过滤全量。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubPageReq extends PageReq<OrderSubDO> {

    /** 店铺 ID（等值匹配，商家端强制注入） */
    @QueryCondition(operator = Operator.EQ)
    private Long shopId;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public OrderSubPageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 构建查询条件：按创建时间倒序。
     *
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<OrderSubDO> buildLambdaWrapper() {
        LambdaQueryWrapper<OrderSubDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(OrderSubDO::getCreateTime);
        return wrapper;
    }
}
