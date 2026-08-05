package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.common.enums.UserTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.UserDO;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

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
