package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.UserProfileDO;

/**
 * 用户扩展资料 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfileDO> {

    /**
     * 按用户 ID 查询扩展资料。
     *
     * @param userId 用户 ID
     * @return 扩展资料，不存在时返回 null
     */
    default UserProfileDO selectByUserId(Long userId) {
        return selectById(userId);
    }

    /**
     * 按用户 ID 删除扩展资料。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int deleteByUserId(Long userId) {
        return delete(Wrappers.<UserProfileDO>lambdaQuery().eq(UserProfileDO::getUserId, userId));
    }
}
