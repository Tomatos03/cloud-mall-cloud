package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.CategoryDO;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {
}
