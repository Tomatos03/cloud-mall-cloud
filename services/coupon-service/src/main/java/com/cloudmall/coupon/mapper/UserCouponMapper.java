package com.cloudmall.coupon.mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.model.entity.UserCouponDO;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.coupon.model.req.UserCouponCursorReq;
import com.cloudmall.coupon.model.req.UserCouponPageReq;

/**
 * 用户券 Mapper。
 * <p>
 * 状态变更全部使用条件更新（乐观锁语义），并发下只有满足前置状态
 * 的行会被修改，影响行数用于判定状态冲突。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCouponDO> {

    /**
     * 分页查询我的优惠券。 <p>
     * 条件由 {@link UserCouponPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 用户券分页数据
     */
    default IPage<UserCouponDO> selectUserCouponPage(UserCouponPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 游标查询我的优惠券。 <p>
     * ID 游标按 ID 降序（新领取的在前）：cursor 为 0 表示首页，之后传上一页最后一条的
     * ID；status 非空时仅查 AVAILABLE（未过期）状态。
     *
     * @param userId 用户 ID
     * @param req 游标请求（status 为空表示全部状态）
     * @param querySize 查询条数（含多查的 1 条）
     * @return 用户券列表
     */
    default List<UserCouponDO> selectUserCouponCursor(Long userId, UserCouponCursorReq req, int querySize) {
        LambdaQueryWrapper<UserCouponDO> wrapper = Wrappers.<UserCouponDO>lambdaQuery()
                .eq(UserCouponDO::getUserId, userId);
        if (req.getStatus() != null) {
            wrapper.eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE);
        }
        Long cursor = ObjectUtil.defaultIfNull(req.getCursor(), 0L);
        if (cursor > 0) {
            wrapper.lt(UserCouponDO::getId, cursor);
        }
        wrapper.orderByDesc(UserCouponDO::getId);
        return selectPage(new Page<>(1, querySize), wrapper).getRecords();
    }

    /**
     * 分页查询锁定过期用户券（按锁定过期时间升序，供释放任务扫描）。 <p>
     * 锁定过期时间早于 {@code now} 且仍为 LOCKED 的用户券视为可安全释放。
     *
     * @param page 分页参数
     * @param now 当前时间
     * @return 锁定过期用户券分页数据
     */
    default IPage<UserCouponDO> selectExpiredLocks(Page<UserCouponDO> page, LocalDateTime now) {
        return selectPage(
                page,
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
                        .isNotNull(UserCouponDO::getLockExpireTime)
                        .lt(UserCouponDO::getLockExpireTime, now)
                        .orderByAsc(UserCouponDO::getLockExpireTime)
        );
    }

    /**
     * 查询订单下全部已锁定用户券（释放/核销场景）。 <p>
     * 条件：锁定订单号匹配且状态为 LOCKED。
     *
     * @param orderNo 订单号
     * @return 已锁定用户券列表
     */
    default List<UserCouponDO> selectLockedByOrder(String orderNo) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
        );
    }

    /**
     * 查询订单下全部已核销用户券（全额退款回退场景）。 <p>
     * 条件：锁定订单号匹配且状态为 USED。
     *
     * @param orderNo 订单号
     * @return 已核销用户券列表
     */
    default List<UserCouponDO> selectUsedByOrder(String orderNo) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.USED)
        );
    }

    /**
     * 查询用户全部可用用户券（候选推荐场景）。
     *
     * @param userId 用户 ID
     * @return 可用用户券列表
     */
    default List<UserCouponDO> selectAvailableByUser(Long userId) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
    }

    /**
     * 统计订单下已锁定用户券数量（幂等判定）。
     *
     * @param orderNo 订单号
     * @return 已锁定用户券数量
     */
    default Long countLockedByOrder(String orderNo) {
        return selectCount(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
        );
    }

    /**
     * 查询指定用户在某订单下已锁定的用户券（幂等重放场景）。
     *
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 已锁定用户券列表
     */
    default List<UserCouponDO> selectLockedByUserAndOrder(Long userId, String orderNo) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
        );
    }

    /**
     * 查询用户在某自动发放事件下已发放的用户券。
     *
     * @param userId 用户 ID
     * @param eventId 发放事件 ID
     * @return 用户券列表
     */
    default List<UserCouponDO> selectByUserAndEvent(Long userId, String eventId) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getIssuedEventId, eventId)
        );
    }

    /**
     * 查询用户已领取的模板 ID 列表。
     *
     * @param userId 用户 ID
     * @param couponIds 模板 ID 集合
     * @return 已领取的模板 ID 列表
     */
    default List<Long> selectClaimedCouponIds(Long userId, Collection<Long> couponIds) {
        if (CollUtil.isEmpty(couponIds)) {
            return List.of();
        }
        List<Object> couponIdValues = selectObjs(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .select(UserCouponDO::getCouponId)
                        .eq(UserCouponDO::getUserId, userId)
                        .in(UserCouponDO::getCouponId, couponIds)
        );
        return couponIdValues.stream()
                             .map(couponId -> (Long) couponId)
                             .toList();
    }

    /**
     * 查询模板下全部可用用户券。
     *
     * @param couponId 模板 ID
     * @return 可用用户券列表
     */
    default List<UserCouponDO> selectAvailableByCoupon(Long couponId) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getCouponId, couponId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
    }

    /**
     * 查询全部已过期但未落库的用户券。
     *
     * @param now 当前时间
     * @return 过期用户券列表
     */
    default List<UserCouponDO> selectOverdue(LocalDateTime now) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
                        .lt(UserCouponDO::getExpireTime, now)
        );
    }

    /**
     * 查询用户已领取的指定模板用户券。
     *
     * @param userId 用户 ID
     * @param couponId 模板 ID
     * @return 用户券（未领取为 null）
     */
    default UserCouponDO selectClaimed(Long userId, Long couponId) {
        return selectOne(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCouponId, couponId)
        );
    }

    /**
     * 查询单个用户的已过期但未落库用户券。
     *
     * @param userId 用户 ID
     * @param now 当前时间
     * @return 过期用户券列表
     */
    default List<UserCouponDO> selectOverdueByUser(Long userId, LocalDateTime now) {
        return selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
                        .lt(UserCouponDO::getExpireTime, now)
        );
    }

    /**
     * 锁定可用用户券：AVAILABLE → LOCKED。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @param orderNo 锁定订单号
     * @param lockExpireTime 锁定过期时间
     * @return 影响行数，0 表示用户券不存在、不属于该用户或已被锁定
     */
    default int lockIfAvailable(Long id, Long userId, String orderNo, LocalDateTime lockExpireTime) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
                        .set(UserCouponDO::getLockedOrderNo, orderNo)
                        .set(UserCouponDO::getLockExpireTime, lockExpireTime)
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
    }

    /**
     * 提交核销已锁定用户券：LOCKED → USED，仅限当前锁定订单。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @param orderNo 锁定订单号
     * @return 影响行数，0 表示状态或订单号不匹配
     */
    default int commitIfLockedByOrder(Long id, Long userId, String orderNo) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.USED)
                        .set(UserCouponDO::getLockedOrderNo, null)
                        .set(UserCouponDO::getLockExpireTime, null)
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
        );
    }

    /**
     * 释放已锁定用户券：LOCKED → AVAILABLE 或 EXPIRED（按过期时间判定），仅限当
     * 前锁定订单。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @param orderNo 锁定订单号
     * @return 影响行数，0 表示状态或订单号不匹配
     */
    default int releaseIfLockedByOrder(Long id, Long userId, String orderNo) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .setSql("status = CASE WHEN expire_time <= NOW() THEN 'EXPIRED' ELSE 'AVAILABLE' END")
                        .set(UserCouponDO::getLockedOrderNo, null)
                        .set(UserCouponDO::getLockExpireTime, null)
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
                        .eq(UserCouponDO::getLockedOrderNo, orderNo)
        );
    }

    /**
     * 全额退款回退已使用用户券：USED → AVAILABLE 或 EXPIRED（按过期时间判定）。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @return 影响行数，0 表示用户券不存在、不属于该用户或状态不是 USED
     */
    default int revertIfUsed(Long id, Long userId) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .setSql("status = CASE WHEN expire_time <= NOW() THEN 'EXPIRED' ELSE 'AVAILABLE' END")
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.USED)
        );
    }

    /**
     * 批量撤销模板下所有可用用户券：AVAILABLE → REVOKED。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    default int revokeAvailableByCoupon(Long couponId) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.REVOKED)
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getCouponId, couponId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
    }

    /**
     * 批量过期：AVAILABLE 且已超过过期时间 → EXPIRED。
     *
     * @return 影响行数
     */
    default int expireOverdue() {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.EXPIRED)
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
                        .apply("expire_time < NOW()")
        );
    }

    /**
     * 单个用户过期落库：查询我的券前实时校验。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int expireOverdueByUser(Long userId) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.EXPIRED)
                        .setSql("state_time = NOW(), update_time = NOW()")
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
                        .apply("expire_time < NOW()")
        );
    }

    /**
     * 单张用户券过期落库：AVAILABLE 且已过期 → EXPIRED（实时校验写库）。
     *
     * @param id 用户券 ID
     * @param now 当前时间
     * @return 影响行数
     */
    default int expireIfOverdue(Long id, LocalDateTime now) {
        return update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.EXPIRED)
                        .set(UserCouponDO::getStateTime, now)
                        .set(UserCouponDO::getUpdateTime, now)
                        .eq(UserCouponDO::getId, id)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
    }
}
