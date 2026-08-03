package com.cloudmall.product.convert;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.cloudmall.product.api.request.UnitCreateReq;
import com.cloudmall.product.api.request.UnitUpdateReq;
import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.entity.UnitDO;

/**
 * 单位对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface UnitConverter {

    /**
     * UnitDO → UnitResp。
     *
     * @param unit 单位实体
     * @return 单位响应
     */
    UnitResp toResp(UnitDO unit);

    /**
     * UnitCreateReq → UnitDO（忽略 ID 和审计字段）。
     *
     * @param req 创建请求
     * @return 单位实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(
            target = "status",
            expression = "java(com.cloudmall.mybatisplus.enums.StatusEnum.ENABLED)"
    )
    @Mapping(
            target = "sortOrder",
            expression = "java(java.util.Optional.ofNullable(req.getSortOrder()).orElse(0))"
    )
    UnitDO toDO(UnitCreateReq req);

    /**
     * 将 {@code req} 中的非 null 字段合并到已有 {@code unit}。 <p>
     * 仅合并 name、sortOrder、status 三个业务字段，null 值不覆盖。
     *
     * @param unit 目标实体（会被修改）
     * @param req 更新请求
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void merge(
            @MappingTarget
            UnitDO unit,
            UnitUpdateReq req
    );
}
