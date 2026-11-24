package com.cloudmall.order.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.model.page.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

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
public class OrderSubPageReq extends PageReq {

    /** 店铺 ID（等值匹配，商家端强制注入） */
    @QueryCondition(operator = OperatorEnum.EQ)
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

}
