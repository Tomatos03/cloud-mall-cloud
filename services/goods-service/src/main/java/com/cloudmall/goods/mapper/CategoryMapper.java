package com.cloudmall.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.goods.entity.CategoryDO;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {
}