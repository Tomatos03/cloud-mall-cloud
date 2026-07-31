package com.cloudmall.user.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.user.entity.AddressDO;

/**
 * 地址分页查询请求。
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
public class AddressPageReq extends PageReq<AddressDO> {

    /** 用户 ID（等值匹配） */
    @QueryCondition(operator = Operator.EQ)
    private Long userId;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public AddressPageReq(long current, long size) {
        super(current, size);
    }
}
