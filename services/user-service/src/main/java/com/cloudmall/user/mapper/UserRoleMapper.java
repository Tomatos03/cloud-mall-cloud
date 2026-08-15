package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.entity.UserRoleDO;

/**
 * 用户-角色关联 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {

    /**
     * 查询用户拥有的全部角色关联。
     *
     * @param userId 用户 ID
     * @return 用户-角色关联列表
     */
    default List<UserRoleDO> selectByUserId(Long userId) {
        return selectList(Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getUserId, userId));
    }

    /**
     * 查询角色下的全部用户关联。
     *
     * @param roleId 角色 ID
     * @return 用户-角色关联列表
     */
    default List<UserRoleDO> selectByRoleId(Long roleId) {
        return selectList(Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getRoleId, roleId));
    }

    /**
     * 删除角色下的全部用户关联。
     *
     * @param roleId 角色 ID
     * @return 影响行数
     */
    default int deleteByRoleId(Long roleId) {
        return delete(Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getRoleId, roleId));
    }

    /**
     * 删除用户拥有的全部角色关联。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int deleteByUserId(Long userId) {
        return delete(Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getUserId, userId));
    }
}
