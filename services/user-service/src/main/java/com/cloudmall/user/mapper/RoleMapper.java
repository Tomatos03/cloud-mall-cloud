package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.entity.RoleDO;
import com.cloudmall.user.model.req.RolePageReq;

/**
 * 角色 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {

    /**
     * 分页查询角色（条件由 {@link RolePageReq} 的 @QueryCondition 注
     * 解声明）。
     *
     * @param req 分页 + 筛选条件
     * @return 角色分页数据
     */
    default IPage<RoleDO> selectRolePage(RolePageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }
}
