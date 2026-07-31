package com.cloudmall.coupon.mapper;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.coupon.entity.UserCouponDO;

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
     * 锁定可用用户券：AVAILABLE → LOCKED。
     *
     * @param id             用户券 ID
     * @param userId         用户 ID
     * @param orderNo        锁定订单号
     * @param lockExpireTime 锁定过期时间
     * @return 影响行数，0 表示用户券不存在、不属于该用户或已被锁定
     */
    @Update("UPDATE cm_user_coupon SET status = 'LOCKED', locked_order_no = #{orderNo}, "
            + "lock_expire_time = #{lockExpireTime}, state_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 'AVAILABLE' AND deleted = 0")
    int lockIfAvailable(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("orderNo") String orderNo,
            @Param("lockExpireTime") LocalDateTime lockExpireTime
    );

    /**
     * 提交核销已锁定用户券：LOCKED → USED，仅限当前锁定订单。
     *
     * @param id      用户券 ID
     * @param userId  用户 ID
     * @param orderNo 锁定订单号
     * @return 影响行数，0 表示状态或订单号不匹配
     */
    @Update("UPDATE cm_user_coupon SET status = 'USED', locked_order_no = NULL, "
            + "lock_expire_time = NULL, state_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 'LOCKED' "
            + "AND locked_order_no = #{orderNo} AND deleted = 0")
    int commitIfLockedByOrder(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("orderNo") String orderNo
    );

    /**
     * 释放已锁定用户券：LOCKED → AVAILABLE 或 EXPIRED（按过期时间判定），仅限当前锁定订单。
     *
     * @param id      用户券 ID
     * @param userId  用户 ID
     * @param orderNo 锁定订单号
     * @return 影响行数，0 表示状态或订单号不匹配
     */
    @Update("UPDATE cm_user_coupon SET status = CASE WHEN expire_time <= NOW() THEN 'EXPIRED' "
            + "ELSE 'AVAILABLE' END, locked_order_no = NULL, lock_expire_time = NULL, "
            + "state_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 'LOCKED' "
            + "AND locked_order_no = #{orderNo} AND deleted = 0")
    int releaseIfLockedByOrder(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("orderNo") String orderNo
    );

    /**
     * 全额退款回退已使用用户券：USED → AVAILABLE 或 EXPIRED（按过期时间判定）。
     *
     * @param id     用户券 ID
     * @param userId 用户 ID
     * @return 影响行数，0 表示用户券不存在、不属于该用户或状态不是 USED
     */
    @Update("UPDATE cm_user_coupon SET status = CASE WHEN expire_time <= NOW() THEN 'EXPIRED' "
            + "ELSE 'AVAILABLE' END, state_time = NOW(), update_time = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 'USED' AND deleted = 0")
    int revertIfUsed(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量撤销模板下所有可用用户券：AVAILABLE → REVOKED。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    @Update("UPDATE cm_user_coupon SET status = 'REVOKED', state_time = NOW(), update_time = NOW() "
            + "WHERE coupon_id = #{couponId} AND status = 'AVAILABLE' AND deleted = 0")
    int revokeAvailableByCoupon(@Param("couponId") Long couponId);

    /**
     * 批量过期：AVAILABLE 且已超过过期时间 → EXPIRED。
     *
     * @return 影响行数
     */
    @Update("UPDATE cm_user_coupon SET status = 'EXPIRED', state_time = NOW(), update_time = NOW() "
            + "WHERE status = 'AVAILABLE' AND expire_time < NOW() AND deleted = 0")
    int expireOverdue();

    /**
     * 单个用户过期落库：查询我的券前实时校验。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Update("UPDATE cm_user_coupon SET status = 'EXPIRED', state_time = NOW(), update_time = NOW() "
            + "WHERE user_id = #{userId} AND status = 'AVAILABLE' AND expire_time < NOW() AND deleted = 0")
    int expireOverdueByUser(@Param("userId") Long userId);
}
