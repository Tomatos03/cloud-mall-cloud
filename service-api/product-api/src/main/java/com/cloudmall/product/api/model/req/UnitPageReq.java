package com.cloudmall.product.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.enums.StatusEnum;
import com.cloudmall.common.model.page.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 单位分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitPageReq extends PageReq {

    /** 状态（等值匹配），默认查询已启用 */
    @QueryCondition(operator = OperatorEnum.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 单位名称（模糊匹配） */
    @QueryCondition(operator = OperatorEnum.LIKE)
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

}
