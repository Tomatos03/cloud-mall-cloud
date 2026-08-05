package com.cloudmall.coupon.mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.coupon.entity.UserCouponDO;
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
     * 查询用户在某自动发放事件下已发放的用户券。
     *
     * @param userId 用户 ID
     * @param eventId 发放事件 ID
     * @return 用户券列表
     */
    @Select(
            """
            SELECT *
            FROM cm_user_coupon
            WHERE user_id = #{userId} AND issued_event_id = #{eventId} AND deleted = 0
            """
    )
    List<UserCouponDO> selectByUserAndEvent(
            @Param("userId")
            Long userId,
            @Param("eventId")
            String eventId
    );

    /**
     * 查询用户已领取的模板 ID 列表。
     *
     * @param userId 用户 ID
     * @param couponIds 模板 ID 集合
     * @return 已领取的模板 ID 列表
     */
    @Select(
            """
            <script>
            SELECT coupon_id
            FROM cm_user_coupon
            WHERE user_id = #{userId} AND deleted = 0
              AND coupon_id IN
            <foreach collection="couponIds" item="couponId" open="(" separator="," close=")">
                #{couponId}
            </foreach>
            </script>
            """
    )
    List<Long> selectClaimedCouponIds(
            @Param("userId")
            Long userId,
            @Param("couponIds")
            Collection<Long> couponIds
    );

    /**
     * 查询模板下全部可用用户券。
     *
     * @param couponId 模板 ID
     * @return 可用用户券列表
     */
    @Select(
            """
            SELECT *
            FROM cm_user_coupon
            WHERE coupon_id = #{couponId} AND status = 'AVAILABLE' AND deleted = 0
            """
    )
    List<UserCouponDO> selectAvailableByCoupon(
            @Param("couponId")
            Long couponId
    );

    /**
     * 查询全部已过期但未落库的用户券。
     *
     * @param now 当前时间
     * @return 过期用户券列表
     */
    @Select(
            """
            SELECT *
            FROM cm_user_coupon
            WHERE status = 'AVAILABLE' AND expire_time < #{now} AND deleted = 0
            """
    )
    List<UserCouponDO> selectOverdue(
            @Param("now")
            LocalDateTime now
    );

    /**
     * 查询用户已领取的指定模板用户券。
     *
     * @param userId 用户 ID
     * @param couponId 模板 ID
     * @return 用户券（未领取为 null）
     */
    @Select(
            """
            SELECT *
            FROM cm_user_coupon
            WHERE user_id = #{userId} AND coupon_id = #{couponId} AND deleted = 0
            LIMIT 1
            """
    )
    UserCouponDO selectClaimed(
            @Param("userId")
            Long userId,
            @Param("couponId")
            Long couponId
    );

    /**
     * 查询单个用户的已过期但未落库用户券。
     *
     * @param userId 用户 ID
     * @param now 当前时间
     * @return 过期用户券列表
     */
    @Select(
            """
            SELECT *
            FROM cm_user_coupon
            WHERE user_id = #{userId}
              AND status = 'AVAILABLE'
              AND expire_time < #{now}
              AND deleted = 0
            """
    )
    List<UserCouponDO> selectOverdueByUser(
            @Param("userId")
            Long userId,
            @Param("now")
            LocalDateTime now
    );

    /**
     * 锁定可用用户券：AVAILABLE → LOCKED。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @param orderNo 锁定订单号
     * @param lockExpireTime 锁定过期时间
     * @return 影响行数，0 表示用户券不存在、不属于该用户或已被锁定
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = 'LOCKED',
                locked_order_no = #{orderNo},
                lock_expire_time = #{lockExpireTime},
                state_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 'AVAILABLE'
              AND deleted = 0
            """
    )
    int lockIfAvailable(
            @Param("id")
            Long id,
            @Param("userId")
            Long userId,
            @Param("orderNo")
            String orderNo,
            @Param("lockExpireTime")
            LocalDateTime lockExpireTime
    );

    /**
     * 提交核销已锁定用户券：LOCKED → USED，仅限当前锁定订单。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @param orderNo 锁定订单号
     * @return 影响行数，0 表示状态或订单号不匹配
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = 'USED',
                locked_order_no = NULL,
                lock_expire_time = NULL,
                state_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 'LOCKED'
              AND locked_order_no = #{orderNo}
              AND deleted = 0
            """
    )
    int commitIfLockedByOrder(
            @Param("id")
            Long id,
            @Param("userId")
            Long userId,
            @Param("orderNo")
            String orderNo
    );

    /**
     * 释放已锁定用户券：LOCKED → AVAILABLE 或 EXPIRED（按过期时间判定），仅限当
     * 前锁定订单。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @param orderNo 锁定订单号
     * @return 影响行数，0 表示状态或订单号不匹配
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = CASE WHEN expire_time <= NOW() THEN 'EXPIRED' ELSE 'AVAILABLE' END,
                locked_order_no = NULL,
                lock_expire_time = NULL,
                state_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 'LOCKED'
              AND locked_order_no = #{orderNo}
              AND deleted = 0
            """
    )
    int releaseIfLockedByOrder(
            @Param("id")
            Long id,
            @Param("userId")
            Long userId,
            @Param("orderNo")
            String orderNo
    );

    /**
     * 全额退款回退已使用用户券：USED → AVAILABLE 或 EXPIRED（按过期时间判定）。
     *
     * @param id 用户券 ID
     * @param userId 用户 ID
     * @return 影响行数，0 表示用户券不存在、不属于该用户或状态不是 USED
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = CASE WHEN expire_time <= NOW() THEN 'EXPIRED' ELSE 'AVAILABLE' END,
                state_time = NOW(),
                update_time = NOW()
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 'USED'
              AND deleted = 0
            """
    )
    int revertIfUsed(
            @Param("id")
            Long id,
            @Param("userId")
            Long userId
    );

    /**
     * 批量撤销模板下所有可用用户券：AVAILABLE → REVOKED。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = 'REVOKED', state_time = NOW(), update_time = NOW()
            WHERE coupon_id = #{couponId} AND status = 'AVAILABLE' AND deleted = 0
            """
    )
    int revokeAvailableByCoupon(
            @Param("couponId")
            Long couponId
    );

    /**
     * 批量过期：AVAILABLE 且已超过过期时间 → EXPIRED。
     *
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = 'EXPIRED', state_time = NOW(), update_time = NOW()
            WHERE status = 'AVAILABLE' AND expire_time < NOW() AND deleted = 0
            """
    )
    int expireOverdue();

    /**
     * 单个用户过期落库：查询我的券前实时校验。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = 'EXPIRED', state_time = NOW(), update_time = NOW()
            WHERE user_id = #{userId}
              AND status = 'AVAILABLE'
              AND expire_time < NOW()
              AND deleted = 0
            """
    )
    int expireOverdueByUser(
            @Param("userId")
            Long userId
    );

    /**
     * 单张用户券过期落库：AVAILABLE 且已过期 → EXPIRED（实时校验写库）。
     *
     * @param id 用户券 ID
     * @param now 当前时间
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_user_coupon
            SET status = 'EXPIRED', state_time = #{now}, update_time = #{now}
            WHERE id = #{id} AND status = 'AVAILABLE' AND deleted = 0
            """
    )
    int expireIfOverdue(
            @Param("id")
            Long id,
            @Param("now")
            LocalDateTime now
    );
}
