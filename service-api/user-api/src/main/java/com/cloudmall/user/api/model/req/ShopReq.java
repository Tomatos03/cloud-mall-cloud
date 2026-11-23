package com.cloudmall.user.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 店铺单个查询请求。
 * <p>
 * 支持按 ID 或用户 ID 查询，通过 {@link QueryCondition @QueryCondition} 注解声明条件，
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ShopReq {

    /** 店铺 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long id;

    /** 用户 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long userId;
}
