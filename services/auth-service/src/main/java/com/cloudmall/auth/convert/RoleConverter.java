package com.cloudmall.auth.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.auth.entity.RoleDO;
import com.cloudmall.auth.model.req.RoleReq;
import com.cloudmall.auth.model.resp.RoleResp;

/**
 * 角色转换器
 * <p>
 * 提供 RoleDO ↔ RoleResp 互转，以及请求到实体的合并更新。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface RoleConverter {

    /**
     * 将数据库实体转换为 RBAC 接口值对象
     *
     * @param roleDo 数据库实体
     * @return RBAC 角色信息值对象
     */
    RoleResp toRoleResp(RoleDO roleDo);

    /**
     * 将请求中的非 null 字段合并到已有实体上。
     *
     * @param req      请求（仅非 null 字段参与合并）
     * @param existing 已有实体，原地更新
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "builtIn", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(RoleReq req, @MappingTarget RoleDO existing);
}
