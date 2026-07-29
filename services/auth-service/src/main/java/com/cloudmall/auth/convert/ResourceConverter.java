package com.cloudmall.auth.convert;

import org.mapstruct.Mapper;

import com.cloudmall.auth.entity.ResourceDO;
import com.cloudmall.auth.model.resp.ResourceResp;

/**
 * ResourceDO → ResourceResp 转换器
 * <p>
 * 同名属性由 MapStruct 自动映射，无需额外配置。
 *
 * @author : Tomatos
 * @date : 2026/7/25
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
}
