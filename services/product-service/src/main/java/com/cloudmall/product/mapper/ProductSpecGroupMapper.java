package com.cloudmall.product.mapper;

import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.model.entity.ProductSpecGroupDO;

/**
 * Product Spec Group Mapper
 *
 * @author cloud-mall
 */
@Mapper
public interface ProductSpecGroupMapper extends BaseMapper<ProductSpecGroupDO> {

    /**
     * 查询商品的规格组，按排序值升序。
     *
     * @param productId 商品 ID
     * @return 规格组列表
     */
    default List<ProductSpecGroupDO> selectByProductId(Long productId) {
        return selectList(
                Wrappers.<ProductSpecGroupDO>lambdaQuery()
                        .eq(ProductSpecGroupDO::getProductId, productId)
                        .orderByAsc(ProductSpecGroupDO::getSort)
        );
    }

    /**
     * 按 ID 集合软删除规格组（空集合直接返回 0）。
     *
     * @param ids 规格组 ID 集合
     * @return 删除条数
     */
    default int deleteGroups(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return deleteBatchIds(ids);
    }
}
