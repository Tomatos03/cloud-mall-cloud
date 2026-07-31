package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.ProductSkuSpecDO;

/**
 * Product SKU Spec Mapper (junction table)
 *
 * @author cloud-mall
 */
@Mapper
public interface ProductSkuSpecMapper extends BaseMapper<ProductSkuSpecDO> {
}
