package com.cloudmall.user.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.RoleResourceDO;

/**
 * 角色-资源关联 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Mapper
public interface RoleResourceMapper extends BaseMapper<RoleResourceDO> {

    /**
     * 查询角色绑定的全部资源关联。
     *
     * @param roleId 角色 ID
     * @return 角色-资源关联列表
     */
    default List<RoleResourceDO> selectByRoleId(Long roleId) {
        return selectList(Wrappers.<RoleResourceDO>lambdaQuery().eq(RoleResourceDO::getRoleId, roleId));
    }

    /**
     * 查询多个角色绑定的全部资源关联。
     *
     * @param roleIds 角色 ID 集合
     * @return 角色-资源关联列表
     */
    default List<RoleResourceDO> selectByRoleIds(Collection<Long> roleIds) {
        return selectList(Wrappers.<RoleResourceDO>lambdaQuery().in(RoleResourceDO::getRoleId, roleIds));
    }

    /**
     * 删除角色绑定的全部资源关联。
     *
     * @param roleId 角色 ID
     * @return 影响行数
     */
    default int deleteByRoleId(Long roleId) {
        return delete(Wrappers.<RoleResourceDO>lambdaQuery().eq(RoleResourceDO::getRoleId, roleId));
    }
}
