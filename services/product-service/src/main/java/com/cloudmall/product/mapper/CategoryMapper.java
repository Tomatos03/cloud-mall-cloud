package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.product.entity.CategoryDO;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {

    /**
     * 查询启用中的分类，按排序值升序（用于构建分类树）。
     *
     * @return 启用分类列表
     */
    default List<CategoryDO> selectEnabledOrdered() {
        return selectList(
                Wrappers.<CategoryDO>lambdaQuery()
                        .eq(CategoryDO::getStatus, StatusEnum.ENABLED)
                        .orderByAsc(CategoryDO::getSortOrder)
        );
    }
}
