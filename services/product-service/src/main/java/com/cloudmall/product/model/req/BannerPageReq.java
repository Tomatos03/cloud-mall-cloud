package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.product.entity.BannerDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * Banner 分页查询请求。
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
public class BannerPageReq extends PageReq<BannerDO> {

    /** 状态（等值匹配），默认查询已启用 */
    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 标题（模糊匹配） */
    @QueryCondition(operator = Operator.LIKE)
    private String title;

    public BannerPageReq(long current, long size) {
        super(current, size);
    }

    @Override
    public LambdaQueryWrapper<BannerDO> buildLambdaWrapper() {
        LambdaQueryWrapper<BannerDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByAsc(BannerDO::getSortOrder);
        return wrapper;
    }
}
