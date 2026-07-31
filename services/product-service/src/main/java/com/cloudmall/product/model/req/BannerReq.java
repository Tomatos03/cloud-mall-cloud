package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.ConditionReq;
import com.cloudmall.product.entity.BannerDO;

/**
 * Banner 列表查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BannerReq extends ConditionReq<BannerDO> {

    /** 状态（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLED;

    /** 标题（模糊匹配） */
    @QueryCondition(operator = Operator.LIKE)
    private String title;

    /**
     * 构建所需对象。
     *
     * @return 处理结果
     */
    @Override
    public LambdaQueryWrapper<BannerDO> buildLambdaWrapper() {
        LambdaQueryWrapper<BannerDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByAsc(BannerDO::getSortOrder);
        return wrapper;
    }
}
