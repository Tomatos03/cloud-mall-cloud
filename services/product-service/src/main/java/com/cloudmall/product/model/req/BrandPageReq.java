package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.product.entity.BrandDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * 品牌分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandPageReq extends PageReq<BrandDO> {

    /** 状态（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 品牌名称（模糊匹配） */
    @QueryCondition(operator = Operator.LIKE)
    private String name;

    /** 品牌英文名称（模糊匹配） */
    @QueryCondition(operator = Operator.LIKE)
    private String nameEn;

    public BrandPageReq(long current, long size) {
        super(current, size);
    }

    @Override
    public LambdaQueryWrapper<BrandDO> buildLambdaWrapper() {
        LambdaQueryWrapper<BrandDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByAsc(BrandDO::getSortOrder);
        return wrapper;
    }
}
