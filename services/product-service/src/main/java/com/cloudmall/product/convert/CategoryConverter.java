package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.request.CategoryCreateReq;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.entity.CategoryDO;

@Mapper(componentModel = "spring")
public interface CategoryConverter {

    CategoryResp toResp(CategoryDO category);

    @Mapping(target = "parentId", defaultExpression = "java(0L)")
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "id", ignore = true)
    CategoryDO toDO(CategoryCreateReq req);
}
