package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.entity.UserDO;
import com.cloudmall.user.model.req.UserPageReq;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 分页查询用户（条件由 {@link UserPageReq} 的 @QueryCondition 注
     * 解声明）。
     *
     * @param req 分页 + 筛选条件
     * @return 用户分页数据
     */
    default IPage<UserDO> selectUserPage(UserPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 按用户名查询用户，软删除过滤由 @TableLogic 自动处理。
     *
     * @param username 用户名
     * @return 用户（不存在为 null）
     */
    default UserDO selectByUsername(String username) {
        return selectOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, username));
    }

    /**
     * 按用户名查询用户是否存在，软删除过滤由 @TableLogic 自动处理。
     *
     * @param username 用户名
     * @return true 表示已存在
     */
    default boolean existsByUsername(String username) {
        Long count = selectCount(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUsername, username));
        return count != null && count > 0;
    }
}
