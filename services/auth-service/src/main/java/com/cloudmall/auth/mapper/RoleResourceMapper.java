package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.RoleResourceDO;

/**
 * 角色-资源关联 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Mapper
public interface RoleResourceMapper extends BaseMapper<RoleResourceDO> {
}
