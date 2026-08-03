package com.cloudmall.product.convert;

import java.util.List;

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
     * 批量转换分类实体列表。
     *
     * @param categories 分类实体列表
     * @return 分类响应列表
     */
    List<CategoryResp> toRespList(List<CategoryDO> categories);

    /**
     * 转换对象数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "parentId", defaultExpression = "java(0L)")
    CategoryDO toDO(CategoryCreateReq req);
}
