package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductSpecDO;

@Mapper
public interface ProductSpecMapper extends BaseMapper<ProductSpecDO> {
}