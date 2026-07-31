package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.product.entity.UnitDO;

/**
 * 单位分页查询请求。
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
public class UnitPageReq extends PageReq<UnitDO> {

    /** 状态（等值匹配），默认查询已启用 */
    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 单位名称（模糊匹配） */
    @QueryCondition(operator = Operator.LIKE)
    private String name;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public UnitPageReq(long current, long size) {
        super(current, size);
    }

    /**
     * 构建所需对象。
     *
     * @return 处理结果
     */
    @Override
    public LambdaQueryWrapper<UnitDO> buildLambdaWrapper() {
        LambdaQueryWrapper<UnitDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByAsc(UnitDO::getSortOrder);
        return wrapper;
    }
}
