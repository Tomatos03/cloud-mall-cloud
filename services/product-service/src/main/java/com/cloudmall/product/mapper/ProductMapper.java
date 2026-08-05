package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.model.req.ProductGetReq;
import com.cloudmall.product.model.req.ProductPageReq;

/**
 * 商品 SPU Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

    /**
     * 按查询条件获取单个 SPU。 <p>
     * 条件由 {@link ProductGetReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 查询条件
     * @return SPU 实体，未命中返回 null
     */
    default ProductDO selectByGetReq(ProductGetReq req) {
        return selectOne(req.buildLambdaWrapper());
    }

    /**
     * 分页查询 SPU，按创建时间倒序。 <p>
     * 条件由 {@link ProductPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return SPU 分页数据
     */
    default IPage<ProductDO> selectProductPage(ProductPageReq req) {
        return selectPage(req, req.buildLambdaWrapper().orderByDesc(ProductDO::getCreateTime));
    }
}
