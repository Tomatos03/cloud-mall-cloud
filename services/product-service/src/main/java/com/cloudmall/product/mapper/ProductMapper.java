package com.cloudmall.product.mapper;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.model.req.ProductGetReq;

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
     * 分页拉取商品搜索文档（按 updateTime 升序，支持增量游标）。
     *
     * @param page 分页参数
     * @param since 增量游标（可选），仅返回 updateTime 晚于 since 的商品
     * @return 搜索文档分页结果
     */
    default IPage<ProductDO> selectSearchDocs(Page<ProductDO> page, LocalDateTime since) {
        LambdaQueryWrapper<ProductDO> wrapper = Wrappers.lambdaQuery();
        if (since != null) {
            wrapper.gt(ProductDO::getUpdateTime, since);
        }
        return selectPage(page, wrapper.orderByAsc(ProductDO::getUpdateTime));
    }
}
