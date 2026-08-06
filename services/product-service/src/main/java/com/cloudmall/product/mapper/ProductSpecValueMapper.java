package com.cloudmall.product.mapper;

import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductSpecValueDO;

/**
 * Product Spec Value Mapper
 *
 * @author cloud-mall
 */
@Mapper
public interface ProductSpecValueMapper extends BaseMapper<ProductSpecValueDO> {

    /**
     * 查询规格组下的规格值，按排序值升序。
     *
     * @param groupId 规格组 ID
     * @return 规格值列表
     */
    default List<ProductSpecValueDO> selectByGroupId(Long groupId) {
        return selectList(
                Wrappers.<ProductSpecValueDO>lambdaQuery()
                        .eq(ProductSpecValueDO::getGroupId, groupId)
                        .orderByAsc(ProductSpecValueDO::getSortOrder)
        );
    }

    /**
     * 按 ID 集合软删除规格值（空集合直接返回 0）。
     *
     * @param ids 规格值 ID 集合
     * @return 删除条数
     */
    default int deleteValues(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return deleteBatchIds(ids);
    }
}
