package com.cloudmall.seckill.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.seckill.enums.SeckillOrderStatusEnum;
import com.cloudmall.seckill.model.entity.SeckillOrderDO;

/**
 * 秒杀购买记录 Mapper
 *
 * @author : Tomatos
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrderDO> {

    /**
     * 按真实订单号查询秒杀单。
     *
     * @param orderNo 真实订单号
     * @return 秒杀单实体，不存在时返回 null
     */
    default SeckillOrderDO selectByOrderNo(String orderNo) {
        return selectOne(Wrappers.<SeckillOrderDO>lambdaQuery().eq(SeckillOrderDO::getOrderNo, orderNo));
    }

    /**
     * 待支付 → 已支付（条件更新防并发）。
     *
     * @param id 秒杀单 ID
     * @return 影响行数，0 表示状态已变更或订单不存在
     */
    default int markPaidIfPending(Long id) {
        return update(
                Wrappers.<SeckillOrderDO>lambdaUpdate()
                        .set(SeckillOrderDO::getStatus, SeckillOrderStatusEnum.PAID)
                        .eq(SeckillOrderDO::getId, id)
                        .eq(SeckillOrderDO::getStatus, SeckillOrderStatusEnum.PENDING_PAYMENT)
        );
    }

    /**
     * 待支付 → 已取消（条件更新防并发）。
     *
     * @param id 秒杀单 ID
     * @return 影响行数，0 表示状态已变更或订单不存在
     */
    default int cancelIfPending(Long id) {
        return update(
                Wrappers.<SeckillOrderDO>lambdaUpdate()
                        .set(SeckillOrderDO::getStatus, SeckillOrderStatusEnum.CANCELLED)
                        .eq(SeckillOrderDO::getId, id)
                        .eq(SeckillOrderDO::getStatus, SeckillOrderStatusEnum.PENDING_PAYMENT)
        );
    }

    /**
     * 扫描超时未支付的秒杀单（创建时间早于阈值）。
     *
     * @param timeoutThreshold 创建时间阈值（早于该时间的视为超时）
     * @param limit 扫描条数上限
     * @return 待回收的秒杀单列表
     */
    default List<SeckillOrderDO> selectTimeoutOrders(LocalDateTime timeoutThreshold, int limit) {
        return selectList(
                Wrappers.<SeckillOrderDO>lambdaQuery()
                        .eq(SeckillOrderDO::getStatus, SeckillOrderStatusEnum.PENDING_PAYMENT)
                        .lt(SeckillOrderDO::getCreateTime, timeoutThreshold)
                        .last(LimitEnum.of(limit))
        );
    }

    /**
     * 查询用户秒杀单（按 ID 倒序）。
     *
     * @param userId 用户 ID
     * @return 秒杀单列表
     */
    default List<SeckillOrderDO> listByUserId(Long userId) {
        return selectList(
                Wrappers.<SeckillOrderDO>lambdaQuery()
                        .eq(SeckillOrderDO::getUserId, userId)
                        .orderByDesc(SeckillOrderDO::getId)
        );
    }
}
