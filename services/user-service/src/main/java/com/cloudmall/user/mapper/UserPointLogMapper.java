package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.user.model.entity.UserPointLogDO;

/**
 * 用户积分流水 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserPointLogMapper extends BaseMapper<UserPointLogDO> {

    /**
     * 按幂等键查询积分流水。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @return 积分流水，不存在时返回 null
     */
    default UserPointLogDO selectByIdempotencyKey(Long userId, String idempotencyKey) {
        return selectOne(
                Wrappers.<UserPointLogDO>lambdaQuery()
                        .eq(UserPointLogDO::getUserId, userId)
                        .eq(UserPointLogDO::getIdempotencyKey, idempotencyKey)
        );
    }

    /**
     * 查询用户最近的积分流水，按时间倒序。
     *
     * @param userId 用户 ID
     * @param limit 条数上限
     * @return 积分流水列表
     */
    default List<UserPointLogDO> listRecent(Long userId, int limit) {
        return selectList(
                Wrappers.<UserPointLogDO>lambdaQuery()
                        .eq(UserPointLogDO::getUserId, userId)
                        .orderByDesc(UserPointLogDO::getCreateTime)
                        .last(LimitEnum.of(Math.max(1, Math.min(limit, 100))))
        );
    }
}
