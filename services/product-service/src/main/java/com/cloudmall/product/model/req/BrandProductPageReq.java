package com.cloudmall.product.model.req;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.product.model.entity.ProductDO;

/**
 * 品牌商品分页查询请求。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
public class BrandProductPageReq extends PageReq<ProductDO> {

    /** 品牌 ID（由路径参数填充） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long brandId;

    /** 商品状态（默认仅上架） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /**
     * 兼容 URL 参数 ?page=。
     *
     * @param page 页码
     */
    public void setPage(int page) {
        setCurrent(page);
    }

    /**
     * 兼容 URL 参数 ?pageSize=。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(int pageSize) {
        setSize(pageSize);
    }

    /**
     * 构建查询条件：按创建时间倒序。
     *
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<ProductDO> buildLambdaWrapper() {
        LambdaQueryWrapper<ProductDO> wrapper = super.buildLambdaWrapper();
        wrapper.orderByDesc(ProductDO::getCreateTime);
        return wrapper;
    }
}
