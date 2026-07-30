package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.product.entity.CommentDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * 评论分页查询请求。
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
public class CommentPageReq extends PageReq<CommentDO> {

    /** 商品 ID（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private Long productId;

    /** 状态（等值匹配），默认查询已启用评论 */
    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    public CommentPageReq(long current, long size) {
        super(current, size);
    }

    @Override
    public LambdaQueryWrapper<CommentDO> buildLambdaWrapper() {
        LambdaQueryWrapper<CommentDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(CommentDO::getCreateTime);
        return wrapper;
    }
}
