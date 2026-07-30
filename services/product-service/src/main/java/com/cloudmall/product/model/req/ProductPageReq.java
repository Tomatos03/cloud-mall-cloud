package com.cloudmall.product.model.req;

import lombok.Getter;
import lombok.Setter;

import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.mybatisplus.annotation.Operator;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * 商品分页查询请求
 * <p>
 * 提供分页参数及商品筛选条件。
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

    public ProductPageReq() {
    }

    /**
     * 兼容 URL 参数 ?page=&size=
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    public ProductPageReq(long current, long size) {
        super(current, size);
    }
}
