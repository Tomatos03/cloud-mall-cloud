package com.cloudmall.coupon.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.coupon.entity.CouponOperationLogDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;

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
public class CouponOperationLogPageReq extends PageReq<CouponOperationLogDO> {

    /** 模板 ID（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private Long couponId;

    /** 操作类型 code（如 PUBLISH/PAUSE/END） */
    @QueryCondition(operator = Operator.EQ)
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

    /**
     * 构建查询条件：按创建时间倒序。
     *
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<CouponOperationLogDO> buildLambdaWrapper() {
        LambdaQueryWrapper<CouponOperationLogDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(CouponOperationLogDO::getCreateTime);
        return wrapper;
    }
}
