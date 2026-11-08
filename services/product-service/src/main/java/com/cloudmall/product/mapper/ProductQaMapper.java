package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.product.model.entity.ProductQaDO;

/**
 * 商品问答 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ProductQaMapper extends BaseMapper<ProductQaDO> {

    /**
     * 查询商品下可见的问答，按有用数与创建时间倒序。
     *
     * @param productId 商品 SPU ID
     * @param limit 返回条数上限
     * @return 问答列表
     */
    default List<ProductQaDO> listVisibleByProduct(Long productId, int limit) {
        return selectList(
                Wrappers.<ProductQaDO>lambdaQuery()
                        .eq(ProductQaDO::getProductId, productId)
                        .eq(ProductQaDO::getStatus, StatusEnum.ENABLED)
                        .orderByDesc(ProductQaDO::getHelpfulCount)
                        .orderByDesc(ProductQaDO::getCreateTime)
                        .last(LimitEnum.of(limit))
        );
    }
}
