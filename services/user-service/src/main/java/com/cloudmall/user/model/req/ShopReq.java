package com.cloudmall.user.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.ConditionReq;
import com.cloudmall.user.model.entity.ShopDO;

/**
 * 店铺单个查询请求。
 * <p>
 * 支持按 ID 或用户 ID 查询，通过 {@link QueryCondition @QueryCondition} 注解声明条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ShopReq extends ConditionReq<ShopDO> {

    /** 店铺 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long id;

    /** 用户 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long userId;
}
