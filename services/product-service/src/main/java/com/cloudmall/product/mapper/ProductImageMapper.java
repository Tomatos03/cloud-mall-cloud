package com.cloudmall.product.mapper;

import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.product.entity.ProductImageDO;

/**
 * Product Image Mapper
 *
 * @author cloud-mall
 */
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImageDO> {

    /**
     * 查询商品图片，主图优先、按排序值升序。
     *
     * @param productId 商品 ID
     * @return 商品图片列表
     */
    default List<ProductImageDO> selectByProductId(Long productId) {
        return selectList(
                Wrappers.<ProductImageDO>lambdaQuery()
                        .eq(ProductImageDO::getProductId, productId)
                        .orderByDesc(ProductImageDO::getIsMain)
                        .orderByAsc(ProductImageDO::getSort)
        );
    }

    /**
     * 查询商品主图（主图优先、按排序值升序取首条）。
     *
     * @param productId 商品 ID
     * @return 主图，商品无图片时返回 null
     */
    default ProductImageDO selectMainImage(Long productId) {
        return selectOne(
                Wrappers.<ProductImageDO>lambdaQuery()
                        .eq(ProductImageDO::getProductId, productId)
                        .eq(ProductImageDO::getIsMain, true)
                        .orderByAsc(ProductImageDO::getSort)
                        .last(LimitEnum.ONE.getSql())
        );
    }

    /**
     * 按商品 ID 集合批量查询各商品主图。
     *
     * @param productIds 商品 ID 集合
     * @return 主图列表，每个商品至多一条
     */
    default List<ProductImageDO> selectMainImagesByProductIds(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return List.of();
        }
        return selectList(
                Wrappers.<ProductImageDO>lambdaQuery()
                        .in(ProductImageDO::getProductId, productIds)
                        .eq(ProductImageDO::getIsMain, true)
                        .orderByAsc(ProductImageDO::getSort)
        );
    }

    /**
     * 删除商品全部图片（物理删除，重建前清空旧图）。
     *
     * @param productId 商品 ID
     * @return 删除条数
     */
    default int deleteByProductId(Long productId) {
        return delete(Wrappers.<ProductImageDO>lambdaQuery().eq(ProductImageDO::getProductId, productId));
    }
}
