package com.cloudmall.goods.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.goods.api.request.CategoryCreateReq;
import com.cloudmall.goods.api.response.CategoryResp;
import com.cloudmall.goods.entity.CategoryDO;

@Mapper(componentModel = "spring")
public interface CategoryConverter {

    CategoryResp toResp(CategoryDO category);

    @Mapping(target = "parentId", defaultExpression = "java(0L)")
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "id", ignore = true)
    CategoryDO toDO(CategoryCreateReq req);
}
