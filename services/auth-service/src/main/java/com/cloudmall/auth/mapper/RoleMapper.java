package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.RoleDO;

/**
 * 角色 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
}
