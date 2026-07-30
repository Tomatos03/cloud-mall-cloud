package com.cloudmall.product.model.req;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.ConditionReq;

/**
 * 商品单个查询请求。
 * <p>
 * 支持按 ID 或 SPU 查询，通过 {@link QueryCondition @QueryCondition} 注解声明条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ProductGetReq extends ConditionReq<ProductDO> {

    /** 商品 ID（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private Long id;

    /** 商品 SPU（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private String spu;

    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status;
}
