package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.user.entity.ResourceDO;
import com.cloudmall.user.model.req.ResourceReq;
import com.cloudmall.user.model.resp.ResourceResp;

/**
 * 资源转换器
 * <p>
 * 提供 ResourceDO ↔ ResourceResp 互转，以及请求到实体的合并更新。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface ResourceConverter {

    /**
     * 将数据库实体转换为 RBAC 接口值对象
     *
     * @param resourceDo 数据库实体
     * @return RBAC 资源信息值对象
     */
    ResourceResp toResourceResp(ResourceDO resourceDo);

    /**
     * 将数据库实体列表转换为 RBAC 接口值对象列表。
     *
     * @param resources 数据库实体列表
     * @return RBAC 资源信息值对象列表
     */
    List<ResourceResp> toResourceRespList(List<ResourceDO> resources);

    /**
     * 将请求中的非 null 字段合并到已有实体上。
     *
     * @param req 请求（仅非 null 字段参与合并）
     * @param existing 已有实体，原地更新
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateEntity(ResourceReq req, @MappingTarget ResourceDO existing);
}
