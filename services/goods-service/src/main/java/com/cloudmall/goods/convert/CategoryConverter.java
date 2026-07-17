package com.cloudmall.goods.convert;

import org.mapstruct.Mapper;

import com.cloudmall.goods.api.response.CategoryResp;
import com.cloudmall.goods.entity.CategoryDO;

@Mapper(componentModel = "spring")
public interface CategoryConverter {

    CategoryResp toResp(CategoryDO category);
}
