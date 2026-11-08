package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.user.model.entity.PointExchangeOrderDO;

/**
 * 积分兑换记录 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface PointExchangeOrderMapper extends BaseMapper<PointExchangeOrderDO> {

    /**
     * 按幂等键查询兑换记录。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @return 兑换记录，不存在时返回 null
     */
    default PointExchangeOrderDO selectByIdempotencyKey(Long userId, String idempotencyKey) {
        return selectOne(
                Wrappers.<PointExchangeOrderDO>lambdaQuery()
                        .eq(PointExchangeOrderDO::getUserId, userId)
                        .eq(PointExchangeOrderDO::getIdempotencyKey, idempotencyKey)
        );
    }

    /**
     * 查询用户对指定商品的兑换成功记录。
     *
     * @param userId 用户 ID
     * @param itemId 兑换商品 ID
     * @return 兑换记录，不存在时返回 null
     */
    default PointExchangeOrderDO selectSuccessByUserAndItem(Long userId, Long itemId) {
        return selectOne(
                Wrappers.<PointExchangeOrderDO>lambdaQuery()
                        .eq(PointExchangeOrderDO::getUserId, userId)
                        .eq(PointExchangeOrderDO::getItemId, itemId)
                        .eq(PointExchangeOrderDO::getStatus, "SUCCESS")
                        .last(LimitEnum.ONE.getSql())
        );
    }
}
