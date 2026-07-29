package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.UserRoleDO;

/**
 * 用户-角色关联 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {
}
