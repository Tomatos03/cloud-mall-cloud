package com.cloudmall.user.mapper;

import java.time.LocalDate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.UserSignInDO;

/**
 * 用户签到 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserSignInMapper extends BaseMapper<UserSignInDO> {

    /**
     * 查询指定日期的签到记录。
     *
     * @param userId 用户 ID
     * @param date 日期
     * @return 签到记录，不存在时返回 null
     */
    default UserSignInDO selectByUserAndDate(Long userId, LocalDate date) {
        return selectOne(
                Wrappers.<UserSignInDO>lambdaQuery()
                        .eq(UserSignInDO::getUserId, userId)
                        .eq(UserSignInDO::getSignDate, date)
        );
    }
}
