package com.cloudmall.product.model.req;

import lombok.Getter;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.product.entity.ProductDO;

/**
 * 商品分页查询请求
 * <p>
 * 提供分页参数及商品筛选条件，筛选条件通过 {@link QueryCondition @QueryCondition}
 * 注解声明，由 {@link #buildLambdaWrapper()} 自动生成查询条件。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Getter
@Setter
public class ProductPageReq extends PageReq<ProductDO> {

    /** 分类 ID（等值查询） */
    @QueryCondition(operator = Operator.EQ)
    private Long categoryId;

    /** 状态（等值匹配），默认查询已启用 */
    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /** 使用指定参数构造对象。 */
    public ProductPageReq() {
    }

    /**
     * 兼容 URL 参数 ?page=&size=
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public ProductPageReq(long current, long size) {
        super(current, size);
    }
}
