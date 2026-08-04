package com.cloudmall.coupon.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.api.response.UserCouponResp;
import com.cloudmall.coupon.cache.CouponCacheService;
import com.cloudmall.coupon.cache.CouponRateLimitService;
import com.cloudmall.coupon.convert.CouponConverter;
import com.cloudmall.coupon.convert.UserCouponConverter;
import com.cloudmall.coupon.convert.UserCouponLogConverter;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.entity.UserCouponLogDO;
import com.cloudmall.coupon.enums.CouponOwnerEnum;
import com.cloudmall.coupon.enums.CouponStatusEnum;
import com.cloudmall.coupon.enums.IssueChannelEnum;
import com.cloudmall.coupon.enums.IssuedSourceEnum;
import com.cloudmall.coupon.enums.UserCouponLogSourceEnum;
import com.cloudmall.coupon.enums.UserCouponOperationTypeEnum;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.coupon.enums.ValidityModeEnum;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponLogMapper;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.model.req.CouponPageReq;
import com.cloudmall.coupon.model.req.UserCouponPageReq;
import com.cloudmall.coupon.service.ICouponTemplateService;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 用户券领域服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UserCouponService implements IUserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final UserCouponLogMapper userCouponLogMapper;
    private final CouponMapper couponMapper;
    private final CouponCacheService couponCacheService;
    private final CouponRateLimitService couponRateLimitService;
    private final CouponConverter couponConverter;
    private final UserCouponConverter userCouponConverter;
    private final UserCouponLogConverter userCouponLogConverter;
    private final ICouponTemplateService couponTemplateService;

    /**
     * 公开领取优惠券。
     *
     * @param couponId 模板 ID
     * @param userId 用户 ID
     * @return 用户券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCouponResp claimPublic(Long couponId, Long userId) {
        AssertUtils.notNull(userId, BizErrorCode.USER_NOT_AUTHENTICATED);
        couponRateLimitService.checkClaimRate(userId);
        CouponDO template = couponTemplateService.getTemplate(couponId);
        UserCouponDO exist = findClaimed(userId, couponId);
        if (exist != null) {
            return userCouponConverter.toResp(exist);
        }
        validateClaimable(template);
        try {
            UserCouponResp resp = issueOne(
                    template,
                    userId,
                    IssuedSourceEnum.PUBLIC_CLAIM,
                    null,
                    userId,
                    UserCouponLogSourceEnum.USER,
                    "公开领取"
            );
            couponCacheService.evictPublicTemplates();
            return resp;
        } catch (DuplicateKeyException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            UserCouponDO claimed = AssertUtils.requireNonNull(
                    findClaimed(userId, couponId),
                    BizErrorCode.COUPON_CLAIM_FAILED
            );
            couponCacheService.evictPublicTemplates();
            return userCouponConverter.toResp(claimed);
        }
    }

    /**
     * 注册自动发放新人券。
     *
     * @param userId 用户 ID
     * @param eventId 注册事件 ID
     * @return 新发放的用户券列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserCouponResp> autoIssueForRegister(Long userId, String eventId) {
        AssertUtils.notNull(userId, BizErrorCode.INVALID_PARAM);
        AssertUtils.assertNotBlank(eventId, BizErrorCode.INVALID_PARAM);
        List<UserCouponDO> issued = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getIssuedEventId, eventId)
        );
        if (CollUtil.isNotEmpty(issued)) {
            return userCouponConverter.toRespList(issued);
        }
        LocalDateTime now = LocalDateTime.now();
        List<CouponDO> templates = couponMapper.selectList(
                Wrappers.<CouponDO>lambdaQuery()
                        .eq(CouponDO::getStatus, CouponStatusEnum.ACTIVE)
                        .eq(CouponDO::getOwnerType, CouponOwnerEnum.PLATFORM)
                        .eq(CouponDO::getIssueChannel, IssueChannelEnum.REGISTER_AUTO)
                        .le(CouponDO::getIssueStartTime, now)
                        .ge(CouponDO::getIssueEndTime, now)
        );
        List<UserCouponResp> result = new ArrayList<>();
        for (CouponDO template : templates) {
            UserCouponDO exist = findClaimed(userId, template.getId());
            if (exist != null) {
                continue;
            }
            result.add(
                    issueOne(
                            template,
                            userId,
                            IssuedSourceEnum.REGISTER_AUTO,
                            eventId,
                            null,
                            UserCouponLogSourceEnum.SYSTEM,
                            "注册自动发放新人券"
                    )
            );
        }
        return result;
    }

    /**
     * 分页查询我的优惠券。
     *
     * @param userId 用户 ID
     * @param req 分页请求（status 为空表示全部）
     * @return 分页结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IPage<UserCouponResp> pageMine(Long userId, UserCouponPageReq req) {
        expireOverdueByUser(userId);
        IPage<UserCouponDO> result = userCouponMapper.selectPage(req, req.buildLambdaWrapper());
        return result.convert(userCouponConverter::toResp);
    }

    /**
     * 分页查询公开券中心（仅 PUBLIC_CLAIM 渠道模板）。
     *
     * @param userId 用户 ID（匿名为 null）
     * @param req 分页请求（name 为空表示全部，issueChannel 由服务层注入）
     * @return 模板分页
     */
    @Override
    public IPage<CouponResp> pagePublic(Long userId, CouponPageReq req) {
        req.setIssueChannel(IssueChannelEnum.PUBLIC_CLAIM);
        IPage<CouponDO> page = couponMapper.selectPage(req, req.buildLambdaWrapper());
        IPage<CouponResp> result = page.convert(couponConverter::toResp);
        enrichPublicClaimState(result.getRecords(), userId);
        return result;
    }

    /**
     * 查询我的单张用户券，校验归属并实时过期。
     *
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @return 用户券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCouponResp getMine(Long userId, Long userCouponId) {
        UserCouponDO uc = requireOwned(userId, userCouponId);
        expireIfOverdue(uc);
        return userCouponConverter.toResp(uc);
    }

    /**
     * 查询公开券中心。
     *
     * @param userId 用户 ID（匿名为 null）
     * @return 模板列表
     */
    @Override
    public List<CouponResp> listPublicCoupons(Long userId) {
        List<CouponResp> anonymous = couponCacheService.getPublicTemplates();
        if (anonymous == null) {
            List<CouponDO> templates = couponMapper.selectList(
                    Wrappers.<CouponDO>lambdaQuery()
                            .eq(CouponDO::getIssueChannel, IssueChannelEnum.PUBLIC_CLAIM)
                            .orderByAsc(CouponDO::getId)
            );
            anonymous = toPublicResps(templates, null);
            couponCacheService.putPublicTemplates(anonymous);
        }
        if (userId == null) {
            return anonymous;
        }
        return refreshClaimState(anonymous, userId);
    }

    /**
     * 刷新用户视角的领取状态与可领状态（基于缓存匿名列表）。
     *
     * @param resps 匿名模板列表
     * @param userId 用户 ID
     * @return 用户视角模板列表
     */
    private List<CouponResp> refreshClaimState(List<CouponResp> resps, Long userId) {
        Set<Long> claimedIds = listClaimedCouponIds(
                userId,
                resps.stream()
                     .map(CouponResp::getId)
                     .toList()
        );
        LocalDateTime now = LocalDateTime.now();
        for (CouponResp resp : resps) {
            boolean claimed = claimedIds.contains(resp.getId());
            resp.setClaimed(claimed);
            resp.setClaimable(resp.isClaimable(now, claimed));
            resp.setStockRemaining(calcStockRemaining(resp));
        }
        return resps;
    }

    /**
     * 查询公开模板详情。
     *
     * @param couponId 模板 ID
     * @param userId 用户 ID（匿名为 null）
     * @return 模板详情
     */
    @Override
    public CouponResp getPublicCoupon(Long couponId, Long userId) {
        CouponResp resp = couponTemplateService.getDetail(couponId);
        if (userId != null) {
            resp.setClaimed(findClaimed(userId, couponId) != null);
        }
        boolean claimed = BooleanUtil.isTrue(resp.getClaimed());
        resp.setClaimable(resp.isClaimable(LocalDateTime.now(), claimed));
        resp.setStockRemaining(calcStockRemaining(resp));
        return resp;
    }

    /**
     * 查询用户已领取的模板 ID 集合。
     *
     * @param userId 用户 ID
     * @param couponIds 模板 ID 列表
     * @return 已领取的模板 ID 集合
     */
    @Override
    public Set<Long> listClaimedCouponIds(Long userId, Collection<Long> couponIds) {
        if (CollUtil.isEmpty(couponIds)) {
            return Collections.emptySet();
        }
        List<UserCouponDO> list = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .in(UserCouponDO::getCouponId, couponIds)
        );
        return list.stream()
                   .map(UserCouponDO::getCouponId)
                   .collect(Collectors.toSet());
    }

    /**
     * 锁定用户券。
     *
     * @param orderNo 锁定订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @param lockExpireTime 锁定过期时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(String orderNo, Long userId, Long userCouponId, LocalDateTime lockExpireTime) {
        AssertUtils.assertNotBlank(orderNo, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userId, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userCouponId, BizErrorCode.INVALID_PARAM);
        String eventId = orderNo + ":" + userCouponId + ":LOCK";
        if (logExists(eventId, UserCouponOperationTypeEnum.LOCK)) {
            return;
        }
        UserCouponDO uc = requireOwned(userId, userCouponId);
        expireIfOverdue(uc);
        AssertUtils.isFalse(
                uc.getStatus() == UserCouponStatusEnum.EXPIRED,
                BizErrorCode.USER_COUPON_EXPIRED
        );
        int affected = userCouponMapper.lockIfAvailable(
                userCouponId,
                userId,
                orderNo,
                lockExpireTime
        );
        if (affected == 0) {
            UserCouponDO fresh = userCouponMapper.selectById(userCouponId);
            if (fresh != null && fresh.getStatus() == UserCouponStatusEnum.LOCKED) {
                throw new BizException(BizErrorCode.USER_COUPON_LOCKED);
            }
            throw new BizException(BizErrorCode.USER_COUPON_STATE_CONFLICT);
        }
        appendLog(
                uc,
                UserCouponOperationTypeEnum.LOCK,
                UserCouponStatusEnum.AVAILABLE,
                UserCouponStatusEnum.LOCKED,
                UserCouponLogSourceEnum.ORDER,
                orderNo,
                eventId,
                null,
                "订单创建锁券"
        );
    }

    /**
     * 提交核销。
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void commit(String orderNo, Long userId, Long userCouponId) {
        AssertUtils.assertNotBlank(orderNo, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userId, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userCouponId, BizErrorCode.INVALID_PARAM);
        String eventId = orderNo + ":" + userCouponId + ":COMMIT";
        if (logExists(eventId, UserCouponOperationTypeEnum.COMMIT)) {
            return;
        }
        UserCouponDO uc = requireOwned(userId, userCouponId);
        int affected = userCouponMapper.commitIfLockedByOrder(userCouponId, userId, orderNo);
        if (affected == 0) {
            UserCouponDO fresh = userCouponMapper.selectById(userCouponId);
            if (fresh != null && fresh.getStatus() == UserCouponStatusEnum.USED) {
                return;
            }
            throw new BizException(BizErrorCode.USER_COUPON_STATE_CONFLICT);
        }
        appendLog(
                uc,
                UserCouponOperationTypeEnum.COMMIT,
                UserCouponStatusEnum.LOCKED,
                UserCouponStatusEnum.USED,
                UserCouponLogSourceEnum.PAYMENT,
                orderNo,
                eventId,
                null,
                "支付成功核销"
        );
    }

    /**
     * 释放锁定。
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @param reason 释放原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(String orderNo, Long userId, Long userCouponId, String reason) {
        AssertUtils.assertNotBlank(orderNo, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userId, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userCouponId, BizErrorCode.INVALID_PARAM);
        String eventId = orderNo + ":" + userCouponId + ":RELEASE";
        if (logExists(eventId, UserCouponOperationTypeEnum.RELEASE)) {
            return;
        }
        UserCouponDO uc = requireOwned(userId, userCouponId);
        int affected = userCouponMapper.releaseIfLockedByOrder(userCouponId, userId, orderNo);
        if (affected == 0) {
            UserCouponDO fresh = AssertUtils.requireNonNull(
                    userCouponMapper.selectById(userCouponId),
                    BizErrorCode.USER_COUPON_NOT_FOUND
            );
            if (fresh.getStatus() == UserCouponStatusEnum.USED) {
                return;
            }
            if (fresh.getStatus() == UserCouponStatusEnum.LOCKED) {
                throw new BizException(BizErrorCode.USER_COUPON_STATE_CONFLICT);
            }
            return;
        }
        UserCouponDO after = userCouponMapper.selectById(userCouponId);
        appendLog(
                uc,
                UserCouponOperationTypeEnum.RELEASE,
                UserCouponStatusEnum.LOCKED,
                after.getStatus(),
                UserCouponLogSourceEnum.ORDER,
                orderNo,
                eventId,
                null,
                ObjectUtil.defaultIfNull(reason, "订单取消/超时释放")
        );
    }

    /**
     * 全额退款回退。
     *
     * @param orderNo 父订单号
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @param refundId 退款单 ID
     * @param reason 回退原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revert(
            String orderNo,
            Long userId,
            Long userCouponId,
            String refundId,
            String reason
    ) {
        AssertUtils.assertNotBlank(orderNo, BizErrorCode.INVALID_PARAM);
        AssertUtils.assertNotBlank(refundId, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userId, BizErrorCode.INVALID_PARAM);
        AssertUtils.notNull(userCouponId, BizErrorCode.INVALID_PARAM);
        String eventId = orderNo + ":" + refundId + ":" + userCouponId + ":REVERT";
        if (logExists(eventId, UserCouponOperationTypeEnum.REVERT)) {
            return;
        }
        UserCouponDO uc = requireOwned(userId, userCouponId);
        int affected = userCouponMapper.revertIfUsed(userCouponId, userId);
        if (affected == 0) {
            UserCouponDO fresh = AssertUtils.requireNonNull(
                    userCouponMapper.selectById(userCouponId),
                    BizErrorCode.USER_COUPON_NOT_FOUND
            );
            if (fresh.isAvailableOrExpired()) {
                return;
            }
            throw new BizException(BizErrorCode.USER_COUPON_STATE_CONFLICT);
        }
        UserCouponDO after = userCouponMapper.selectById(userCouponId);
        appendLog(
                uc,
                UserCouponOperationTypeEnum.REVERT,
                UserCouponStatusEnum.USED,
                after.getStatus(),
                UserCouponLogSourceEnum.PAYMENT,
                orderNo,
                eventId,
                null,
                ObjectUtil.defaultIfNull(reason, "全额退款回退")
        );
    }

    /**
     * 运营撤销模板下全部可用用户券。
     *
     * @param couponId 模板 ID
     * @param operatorId 操作人 ID
     * @param reason 撤销原因
     * @return 撤销数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeByCoupon(Long couponId, Long operatorId, String reason) {
        AssertUtils.isTrue(UserContextHolder.isAdmin(), BizErrorCode.COUPON_OPERATION_NOT_ALLOWED);
        List<UserCouponDO> available = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getCouponId, couponId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
        if (CollUtil.isEmpty(available)) {
            return 0;
        }
        int affected = userCouponMapper.revokeAvailableByCoupon(couponId);
        userCouponLogMapper.insert(
                userCouponLogConverter.toRevokeLogList(available, operatorId, reason)
        );
        return affected;
    }

    /**
     * 批量过期任务。
     *
     * @return 过期数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireOverdue() {
        List<UserCouponDO> overdue = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
                        .lt(UserCouponDO::getExpireTime, LocalDateTime.now())
        );
        if (CollUtil.isEmpty(overdue)) {
            return 0;
        }
        int affected = userCouponMapper.expireOverdue();
        appendExpireLogs(overdue);
        return affected;
    }

    /**
     * 校验模板可领取性（公开领取前置校验）。
     *
     * @param template 模板
     */
    private void validateClaimable(CouponDO template) {
        AssertUtils.isTrue(template.isPublicClaimChannel(), BizErrorCode.COUPON_NOT_CLAIMABLE);
        AssertUtils.isTrue(
                template.getStatus() == CouponStatusEnum.ACTIVE,
                BizErrorCode.COUPON_NOT_CLAIMABLE
        );
        LocalDateTime now = LocalDateTime.now();
        AssertUtils.isFalse(template.isNotStarted(now), BizErrorCode.COUPON_NOT_STARTED);
        AssertUtils.isFalse(template.isEnded(now), BizErrorCode.COUPON_ISSUE_ENDED);
        if (template.getTotalCount() != null) {
            AssertUtils.isTrue(template.isStockAvailable(), BizErrorCode.COUPON_STOCK_NOT_ENOUGH);
        }
    }

    /**
     * 发放单张用户券（原子递增发行数量 + 插入用户券 + 插入流水，同一事务）。
     *
     * @param template 模板
     * @param userId 用户 ID
     * @param source 发放来源
     * @param eventId 自动发放事件 ID（公开领取为 null）
     * @param operatorId 操作人 ID
     * @param logSource 流水来源
     * @param reason 发放原因
     * @return 用户券
     */
    private UserCouponResp issueOne(
            CouponDO template,
            Long userId,
            IssuedSourceEnum source,
            String eventId,
            Long operatorId,
            UserCouponLogSourceEnum logSource,
            String reason
    ) {
        int affected = couponMapper.increaseClaimedCount(template.getId());
        AssertUtils.isNotZero(affected, BizErrorCode.COUPON_STOCK_NOT_ENOUGH);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effective;
        LocalDateTime expire;
        if (template.getValidityMode() == ValidityModeEnum.FIXED) {
            effective = template.getFixedStartTime();
            expire = template.getFixedExpireTime();
        } else {
            effective = now;
            expire = now.plusDays(template.getValidDays());
        }
        UserCouponDO uc = userCouponConverter.toIssue(
                template,
                userId,
                source,
                eventId,
                now,
                effective,
                expire
        );
        userCouponMapper.insert(uc);
        userCouponLogMapper.insert(
                userCouponLogConverter.toLog(
                        uc,
                        UserCouponOperationTypeEnum.ISSUE,
                        null,
                        UserCouponStatusEnum.AVAILABLE,
                        logSource,
                        null,
                        source == IssuedSourceEnum.REGISTER_AUTO
                                ? eventId + ":" + template.getId()
                                : null,
                        operatorId,
                        reason
                )
        );
        return userCouponConverter.toResp(uc);
    }

    /**
     * 查询用户已领取的指定模板用户券。
     *
     * @param userId 用户 ID
     * @param couponId 模板 ID
     * @return 用户券（未领取为 null）
     */
    private UserCouponDO findClaimed(Long userId, Long couponId) {
        return userCouponMapper.selectOne(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCouponId, couponId)
        );
    }

    /**
     * 单个用户过期落库并写流水（我的券查询前实时校验）。
     *
     * @param userId 用户 ID
     */
    private void expireOverdueByUser(Long userId) {
        List<UserCouponDO> overdue = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
                        .lt(UserCouponDO::getExpireTime, LocalDateTime.now())
        );
        if (CollUtil.isEmpty(overdue)) {
            return;
        }
        userCouponMapper.expireOverdueByUser(userId);
        appendExpireLogs(overdue);
    }

    /**
     * 单张用户券实时过期校验：AVAILABLE 且已过期则落库并写流水。
     *
     * @param uc 用户券
     */
    private void expireIfOverdue(UserCouponDO uc) {
        if (!uc.isOverdue()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        userCouponMapper.update(
                Wrappers.<UserCouponDO>lambdaUpdate()
                        .set(UserCouponDO::getStatus, UserCouponStatusEnum.EXPIRED)
                        .set(UserCouponDO::getStateTime, now)
                        .set(UserCouponDO::getUpdateTime, now)
                        .eq(UserCouponDO::getId, uc.getId())
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
        uc.setStatus(UserCouponStatusEnum.EXPIRED);
        userCouponLogMapper.insert(userCouponLogConverter.toExpireLog(uc, "实时过期校验"));
    }

    /**
     * 批量写过期流水。
     *
     * @param overdue 过期用户券列表
     */
    private void appendExpireLogs(List<UserCouponDO> overdue) {
        userCouponLogMapper.insert(userCouponLogConverter.toExpireLogList(overdue, "批量过期任务"));
    }

    /**
     * 校验用户券归属并返回实体。
     *
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @return 用户券
     */
    private UserCouponDO requireOwned(Long userId, Long userCouponId) {
        UserCouponDO uc = AssertUtils.requireNonNull(
                userCouponMapper.selectById(userCouponId),
                BizErrorCode.USER_COUPON_NOT_FOUND
        );
        AssertUtils.isEqual(uc.getUserId(), userId, BizErrorCode.USER_COUPON_NOT_BELONG_TO_USER);
        return uc;
    }

    /**
     * 判断事件是否已处理（流水幂等）。
     *
     * @param eventId 事件 ID
     * @param op 操作类型
     * @return true 为已处理
     */
    private boolean logExists(String eventId, UserCouponOperationTypeEnum op) {
        Long count = userCouponLogMapper.selectCount(
                Wrappers.<UserCouponLogDO>lambdaQuery()
                        .eq(UserCouponLogDO::getEventId, eventId)
                        .eq(UserCouponLogDO::getOperationType, op)
        );
        return count != null && count > 0;
    }

    /**
     * 追加用户券状态流水。
     *
     * @param uc 用户券
     * @param op 操作类型
     * @param from 原状态
     * @param to 新状态
     * @param source 来源
     * @param orderNo 订单号
     * @param eventId 事件 ID
     * @param operatorId 操作人 ID
     * @param reason 原因
     */
    private void appendLog(
            UserCouponDO uc,
            UserCouponOperationTypeEnum op,
            UserCouponStatusEnum from,
            UserCouponStatusEnum to,
            UserCouponLogSourceEnum source,
            String orderNo,
            String eventId,
            Long operatorId,
            String reason
    ) {
        userCouponLogMapper.insert(
                userCouponLogConverter.toLog(
                        uc,
                        op,
                        from,
                        to,
                        source,
                        orderNo,
                        eventId,
                        operatorId,
                        reason
                )
        );
    }

    /**
     * 模板列表转换为公开响应（计算 claimed/claimable/stock_remaining）。
     *
     * @param templates 模板列表
     * @param userId 用户 ID（匿名为 null）
     * @return 响应列表
     */
    private List<CouponResp> toPublicResps(List<CouponDO> templates, Long userId) {
        List<CouponResp> resps = couponConverter.toRespList(templates);
        enrichPublicClaimState(resps, userId);
        return resps;
    }

    /**
     * 填充公开券中心模板的用户领取状态与可领状态。 <p>
     * 匿名用户（userId 为 null）claimed 为 null，仅计算
     * stockRemaining。
     *
     * @param resps 模板响应列表（原地修改）
     * @param userId 用户 ID（匿名为 null）
     */
    private void enrichPublicClaimState(List<CouponResp> resps, Long userId) {
        Set<Long> claimedIds = userId == null
                ? Collections.emptySet()
                : listClaimedCouponIds(userId, resps.stream()
                                                    .map(CouponResp::getId)
                                                    .toList());
        LocalDateTime now = LocalDateTime.now();
        for (CouponResp resp : resps) {
            boolean claimed = claimedIds.contains(resp.getId());
            resp.setClaimed(userId == null ? null : claimed);
            resp.setClaimable(resp.isClaimable(now, claimed));
            resp.setStockRemaining(calcStockRemaining(resp));
        }
    }

    /**
     * 计算剩余可领取数量（不限量为 null）。
     *
     * @param resp 模板响应
     * @return 剩余数量
     */
    private Long calcStockRemaining(CouponResp resp) {
        if (resp.getTotalCount() == null) {
            return null;
        }
        long claimed = ObjectUtil.defaultIfNull(resp.getClaimedCount(), 0L);
        return Math.max(0L, resp.getTotalCount() - claimed);
    }

}
