package com.cloudmall.product.mapper;

import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductSkuSpecDO;

/**
 * Product SKU Spec Mapper (junction table)
 *
 * @author cloud-mall
 */
@Mapper
public interface ProductSkuSpecMapper extends BaseMapper<ProductSkuSpecDO> {

    /**
     * 查询多个 SKU 的规格值关联。
     *
     * @param skuIds SKU ID 集合
     * @return SKU-规格值关联列表
     */
    default List<ProductSkuSpecDO> selectBySkuIds(Collection<Long> skuIds) {
        return selectList(Wrappers.<ProductSkuSpecDO>lambdaQuery().in(ProductSkuSpecDO::getSkuId, skuIds));
    }

    /**
     * 删除多个 SKU 的规格值关联（物理删除，重建关联前清空旧行）。
     *
     * @param skuIds SKU ID 集合
     * @return 删除条数
     */
    default int deleteBySkuIds(Collection<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return 0;
        }
        return delete(Wrappers.<ProductSkuSpecDO>lambdaQuery().in(ProductSkuSpecDO::getSkuId, skuIds));
    }

    /**
     * 查询指定规格值集合下、且属于指定商品的 SKU 规格值关联。
     *
     * @param specValueIds 规格值 ID 集合
     * @param productId 商品 ID
     * @return SKU-规格值关联列表
     */
    default List<ProductSkuSpecDO> selectBySpecValueIdsInProduct(
        Collection<Long> specValueIds,
        Long productId
    ) {
        return selectList(
                Wrappers.<ProductSkuSpecDO>lambdaQuery()
                        .in(ProductSkuSpecDO::getSpecValueId, specValueIds)
                        .inSql(
                                ProductSkuSpecDO::getSkuId,
                                "SELECT id FROM cm_product_sku WHERE product_id = " + productId
                        )
        );
    }
}
