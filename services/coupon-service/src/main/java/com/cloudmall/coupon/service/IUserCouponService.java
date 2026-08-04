package com.cloudmall.coupon.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.api.response.UserCouponResp;
import com.cloudmall.coupon.model.req.CouponPageReq;
import com.cloudmall.coupon.model.req.UserCouponPageReq;

/**
 * 用户券领域服务
 * <p>
 * 负责公开领取、新人自动发放、我的券查询、公开券中心查询，
 * 以及 AVAILABLE → LOCKED → USED 生命周期状态机和状态流水幂等。
 *
 * @author : Tomatos
 */
public interface IUserCouponService {

    /**
     * 公开领取优惠券。 <p>
     * 同一用户同一模板重复领取幂等返回原用户券，不重复扣库存。
     *
     * @param couponId 模板 ID
     * @param userId 用户 ID
     * @return 用户券
     */
    UserCouponResp claimPublic(Long couponId, Long userId);

    /**
     * 注册自动发放新人券。 <p>
     * 按事件 ID 幂等，同一事件重复调用返回已发放的用户券。
     *
     * @param userId 用户 ID
     * @param eventId 注册事件 ID
     * @return 新发放的用户券列表
     */
    List<UserCouponResp> autoIssueForRegister(Long userId, String eventId);

    /**
     * 分页查询我的优惠券，查询前先对当前用户做实时过期落库。
     *
     * @param userId 用户 ID
     * @param req 分页请求（status 为空表示全部）
     * @return 分页结果
     */
    IPage<UserCouponResp> pageMine(Long userId, UserCouponPageReq req);

    /**
     * 分页查询公开券中心（仅 PUBLIC_CLAIM 渠道模板），附带
     * claimed/claimable/stock_remaining。
     *
     * @param userId 用户 ID（匿名为 null）
     * @param req 分页请求（name 为空表示全部，issueChannel 由服务层注入）
     * @return 模板分页
     */
    IPage<CouponResp> pagePublic(Long userId, CouponPageReq req);

    /**
     * 查询我的单张用户券，校验归属并实时过期。
     *
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @return 用户券
     */
    UserCouponResp getMine(Long userId, Long userCouponId);

    /**
     * 查询公开券中心（仅 PUBLIC_CLAIM 渠道模板），附带
     * claimed/claimable/stock_remaining。
     *
     * @param userId 用户 ID（匿名为 null）
     * @return 模板列表
     */
    List<CouponResp> listPublicCoupons(Long userId);

    /**
     * 查询公开模板详情，附带 claimed/claimable/stock_remaining 和范围。
     *
     * @param couponId 模板 ID
     * @param userId 用户 ID（匿名为 null）
     * @return 模板详情
     */
    CouponResp getPublicCoupon(Long couponId, Long userId);

    /**
     * 查询用户已领取的模板 ID 集合。
     *
     * @param userId 用户 ID
     * @param couponIds 模板 ID 列表
     * @return 已领取的模板 ID 集合
     */
    Set<Long> listClaimedCouponIds(Long userId, Collection<Long> couponIds);

    /**
     * 锁定用户券：AVAILABLE → LOCKED，按订单号幂等。
     *
     * @param orderNo 锁定订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @param lockExpireTime 锁定过期时间
     */
    void lock(String orderNo, Long userId, Long userCouponId, LocalDateTime lockExpireTime);

    /**
     * 提交核销：LOCKED → USED，按订单号幂等。
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     */
    void commit(String orderNo, Long userId, Long userCouponId);

    /**
     * 释放锁定：LOCKED → AVAILABLE/EXPIRED，按订单号幂等。
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @param reason 释放原因
     */
    void release(String orderNo, Long userId, Long userCouponId, String reason);

    /**
     * 全额退款回退：USED → AVAILABLE/EXPIRED，按订单号+退款单幂等。
     *
     * @param orderNo 父订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @param refundId 退款单 ID
     * @param reason 回退原因
     */
    void revert(String orderNo, Long userId, Long userCouponId, String refundId, String reason);

    /**
     * 运营撤销模板下全部可用用户券：AVAILABLE → REVOKED。
     *
     * @param couponId 模板 ID
     * @param operatorId 操作人 ID
     * @param reason 撤销原因
     * @return 撤销数量
     */
    int revokeByCoupon(Long couponId, Long operatorId, String reason);

    /**
     * 批量过期任务：AVAILABLE 且已超过过期时间 → EXPIRED。
     *
     * @return 过期数量
     */
    int expireOverdue();
}
