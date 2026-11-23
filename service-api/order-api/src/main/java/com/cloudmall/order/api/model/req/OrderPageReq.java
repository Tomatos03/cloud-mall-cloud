package com.cloudmall.order.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.model.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 订单分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageReq extends PageReq {

    /** 用户 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long userId;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public OrderPageReq(long current, long size) {
        super(current, size);
    }

}
