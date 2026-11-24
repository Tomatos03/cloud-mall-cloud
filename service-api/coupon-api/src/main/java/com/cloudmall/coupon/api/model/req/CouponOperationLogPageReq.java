package com.cloudmall.coupon.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.model.page.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 模板操作流水分页查询请求
 * <p>
 * 运营端审计查询，可按模板与操作类型过滤。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponOperationLogPageReq extends PageReq {

    /** 模板 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long couponId;

    /** 操作类型 code（如 PUBLISH/PAUSE/END） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private String actionType;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public CouponOperationLogPageReq(long current, long size) {
        super(current, size);
    }

}
