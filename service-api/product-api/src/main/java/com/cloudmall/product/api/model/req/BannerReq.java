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
 * Banner 列表查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class BannerReq {

    /** 状态（等值匹配） */
    @Builder.Default
    @QueryCondition(operator = OperatorEnum.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 标题（模糊匹配） */
    @QueryCondition(operator = OperatorEnum.LIKE)
    private String title;

}
