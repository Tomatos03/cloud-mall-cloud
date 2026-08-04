package com.cloudmall.product.convert;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.cloudmall.product.api.request.BrandCreateReq;
import com.cloudmall.product.api.request.BrandUpdateReq;
import com.cloudmall.product.api.response.BrandResp;
import com.cloudmall.product.entity.BrandDO;

/**
 * 品牌对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface BrandConverter {

    /**
     * BrandDO → BrandResp。
     *
     * @param brand 品牌实体
     * @return 品牌响应
     */
    @Mapping(target = "brandId", source = "id")
    BrandResp toResp(BrandDO brand);

    /**
     * BrandDO 列表 → BrandResp 列表。
     *
     * @param brands 品牌实体列表
     * @return 品牌响应列表
     */
    List<BrandResp> toRespList(List<BrandDO> brands);

    /**
     * BrandCreateReq → BrandDO。
     *
     * @param req 创建请求
     * @return 品牌实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "status", expression = "java(com.cloudmall.mybatisplus.enums.StatusEnum.ENABLED)")
    @Mapping(
            target = "sortOrder",
            expression = "java(java.util.Optional.ofNullable(req.getSortOrder()).orElse(0))"
    )
    BrandDO toDO(BrandCreateReq req);

    /**
     * 将 {@code req} 中的非 null 字段合并到已有 {@code brand}。
     *
     * @param brand 目标实体
     * @param req 更新请求
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(
            @MappingTarget
            BrandDO brand,
            BrandUpdateReq req
    );
}
