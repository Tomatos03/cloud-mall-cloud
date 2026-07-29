package com.cloudmall.goods.model.req;

import lombok.Getter;
import lombok.Setter;

import com.cloudmall.goods.entity.GoodsDO;
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
public class GoodsPageReq extends PageReq<GoodsDO> {

    /** 分类 ID（等值查询） */
    @QueryCondition(operator = Operator.EQ)
    private Long categoryId;

    public GoodsPageReq() {
    }

    /**
     * 兼容 URL 参数 ?page=&size=
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    public GoodsPageReq(long current, long size) {
        super(current, size);
    }
}
