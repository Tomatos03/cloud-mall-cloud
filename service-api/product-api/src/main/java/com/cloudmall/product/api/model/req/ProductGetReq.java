package com.cloudmall.product.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.common.enums.StatusEnum;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 商品单个查询请求。
 * <p>
 * 支持按 ID 或 SPU 查询，通过 {@link QueryCondition @QueryCondition} 注解声明条件，
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ProductGetReq {

    /** 商品 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long id;

    /** 商品 SPU 编码（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private String spuCode;

    @QueryCondition(operator = OperatorEnum.EQ)
    private StatusEnum status;
}
