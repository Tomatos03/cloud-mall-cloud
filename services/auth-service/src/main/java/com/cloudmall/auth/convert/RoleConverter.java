package com.cloudmall.auth.convert;

import org.mapstruct.Mapper;

import com.cloudmall.auth.entity.RoleDO;
import com.cloudmall.auth.model.resp.RoleResp;

/**
 * RoleDO → RoleResp 转换器
 * <p>
 * 同名属性由 MapStruct 自动映射。
 *
 * @author : Tomatos
 * @date : 2026/7/29
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
}
