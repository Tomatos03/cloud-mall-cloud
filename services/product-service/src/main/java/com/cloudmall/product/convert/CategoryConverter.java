package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.request.CategoryCreateReq;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.entity.CategoryDO;

@Mapper(componentModel = "spring")
public interface CategoryConverter {

    /**
     * 转换对象数据。
     *
     * @param category 分类实体
     * @return 处理结果
     */
    CategoryResp toResp(CategoryDO category);

    /**
     * 转换对象数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @Mapping(target = "parentId", defaultExpression = "java(0L)")
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "id", ignore = true)
    CategoryDO toDO(CategoryCreateReq req);
}
