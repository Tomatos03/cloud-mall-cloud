package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

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
                        .orderByAsc(ProductImageDO::getSortOrder)
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
