package com.cloudmall.coupon.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.api.request.CouponOrderCommitReq;
import com.cloudmall.coupon.api.request.CouponOrderLockReq;
import com.cloudmall.coupon.api.request.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.request.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.request.CouponOrderRevertReq;
import com.cloudmall.coupon.api.response.CouponCandidateResp;
import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.coupon.api.response.CouponUsageResult;
import com.cloudmall.coupon.calc.CalcCoupon;
import com.cloudmall.coupon.calc.CouponCalculator;
import com.cloudmall.coupon.calc.CouponScopeMatcher;
import com.cloudmall.coupon.convert.CouponOrderConverter;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.entity.UserCouponLogDO;
import com.cloudmall.coupon.enums.UserCouponOperationTypeEnum;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponLogMapper;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.service.ICouponOrderService;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 订单协同内部服务实现
 * <p>
 * 校验用户券归属与状态（只读，不落库），范围匹配后由计算器
 * 计算三级优惠；lock 在同一事务内锁定用户选择的券；
 * 推荐组合枚举合法候选券的组合并取优惠最大、过期更早的一组。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CouponOrderService implements ICouponOrderService {

    private static final String DEFAULT_RELEASE_REASON = "订单取消/超时释放";
    private static final String DEFAULT_REVERT_REASON = "全额退款回退";

    private final UserCouponMapper userCouponMapper;
    private final UserCouponLogMapper userCouponLogMapper;
    private final CouponMapper couponMapper;
    private final CouponScopeMatcher scopeMatcher;
    private final CouponCalculator calculator;
    private final CouponOrderConverter couponOrderConverter;
    private final IUserCouponService userCouponService;

    /**
     * 无副作用试算。
     *
     * @param req 试算请求
     * @return 优惠快照
     */
    @Override
    public CouponOrderCalcResp quote(CouponOrderQuoteReq req) {
        CouponOrderCalcResp resp = doCalculate(req);
        fillCandidates(resp, req.getItems(), req.getUserId());
        return resp;
    }

    /**
     * 锁定：先计算，再锁定用户选择的券。
     *
     * @param req 锁定请求
     * @return 优惠快照
     */
    @Override
    @Transactional
    public CouponOrderCalcResp lock(CouponOrderLockReq req) {
        AssertUtils.assertNotBlank(req.getOrderNo(), BizErrorCode.INVALID_PARAM);
        if (hasLocked(req.getOrderNo())) {
            return rebuildByOrder(req);
        }
        CouponOrderCalcResp resp = doCalculate(req);
        lockSelected(req, resp);
        return resp;
    }

    /**
     * 支付成功核销。
     *
     * @param req 核销请求
     */
    @Override
    @Transactional
    public void commit(CouponOrderCommitReq req) {
        userCouponService.commit(req.getOrderNo(), req.getUserId(), req.getUserCouponId());
    }

    /**
     * 取消/超时释放。
     *
     * @param req 释放请求
     */
    @Override
    @Transactional
    public void release(CouponOrderReleaseReq req) {
        userCouponService.release(
                req.getOrderNo(),
                req.getUserId(),
                req.getUserCouponId(),
                Objects.requireNonNullElse(req.getReason(), DEFAULT_RELEASE_REASON)
        );
    }

    /**
     * 全额退款回退。
     *
     * @param req 回退请求
     */
    @Override
    @Transactional
    public void revert(CouponOrderRevertReq req) {
        userCouponService.revert(
                req.getOrderNo(),
                req.getUserId(),
                req.getUserCouponId(),
                req.getRefundId(),
                Objects.requireNonNullElse(req.getReason(), DEFAULT_REVERT_REASON)
        );
    }

    /**
     * 确认计算用户选择的券（不含候选推荐）。
     *
     * @param req 请求
     * @return 优惠快照
     */
    private CouponOrderCalcResp doCalculate(CouponOrderQuoteReq req) {
        List<CouponCalcItem> items = req.getItems();
        AssertUtils.notEmpty(items, BizErrorCode.INVALID_PARAM);
        CalcCoupon platformCoupon = resolveSelectedPlatform(
                req.getUserId(),
                req.getPlatformUserCouponId(),
                items
        );
        List<CalcCoupon> shopCoupons = resolveSelectedShops(
                req.getUserId(),
                req.getShopUserCouponIds(),
                items
        );
        return calculator.calculate(items, platformCoupon, shopCoupons);
    }

    /**
     * 解析用户选择的平台券。
     *
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID（可空）
     * @param items 商品明细
     * @return 计算券（未选择为 null）
     */
    private CalcCoupon resolveSelectedPlatform(
            Long userId,
            Long userCouponId,
            List<CouponCalcItem> items
    ) {
        if (userCouponId == null) {
            return null;
        }
        UserCouponDO uc = requireUsable(userId, userCouponId);
        AssertUtils.isTrue(uc.isPlatform(), BizErrorCode.COUPON_OWNER_INVALID);
        return toCalcCoupon(uc, items);
    }

    /**
     * 解析用户选择的店铺券列表。
     *
     * @param userId 用户 ID
     * @param shopUserCouponIds 店铺券选择（shopId →
     *                          userCouponId）
     * @param items 商品明细
     * @return 计算券列表
     */
    private List<CalcCoupon> resolveSelectedShops(
            Long userId,
            Map<Long, Long> shopUserCouponIds,
            List<CouponCalcItem> items
    ) {
        if (CollUtil.isEmpty(shopUserCouponIds)) {
            return Collections.emptyList();
        }
        List<CalcCoupon> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : shopUserCouponIds.entrySet()) {
            Long ucId = entry.getValue();
            if (ucId == null) {
                continue;
            }
            UserCouponDO uc = requireUsable(userId, ucId);
            AssertUtils.isTrue(uc.isShop(), BizErrorCode.COUPON_OWNER_INVALID);
            AssertUtils.isEqual(uc.getShopId(), entry.getKey(), BizErrorCode.COUPON_OWNER_INVALID);
            result.add(toCalcCoupon(uc, items));
        }
        return result;
    }

    /**
     * 校验用户券归属与可用状态（只读，不落库过期）。
     *
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID
     * @return 用户券
     */
    private UserCouponDO requireUsable(Long userId, Long userCouponId) {
        UserCouponDO uc = userCouponMapper.selectById(userCouponId);
        AssertUtils.notNull(uc, BizErrorCode.USER_COUPON_NOT_FOUND);
        AssertUtils.isEqual(uc.getUserId(), userId, BizErrorCode.USER_COUPON_NOT_BELONG_TO_USER);
        if (uc.isOverdue()) {
            throw new BizException(BizErrorCode.USER_COUPON_EXPIRED);
        }
        if (uc.isLocked()) {
            throw new BizException(BizErrorCode.USER_COUPON_LOCKED);
        }
        if (uc.isUsed()) {
            throw new BizException(BizErrorCode.USER_COUPON_USED);
        }
        AssertUtils.isTrue(uc.isAvailable(), BizErrorCode.USER_COUPON_STATE_CONFLICT);
        return uc;
    }

    /**
     * 组装计算券（规则参数来自用户券快照，范围匹配用模板）。
     *
     * @param uc 用户券
     * @param items 商品明细
     * @return 计算券
     */
    private CalcCoupon toCalcCoupon(UserCouponDO uc, List<CouponCalcItem> items) {
        CouponDO template = couponMapper.selectById(uc.getCouponId());
        AssertUtils.notNull(template, BizErrorCode.COUPON_TEMPLATE_NOT_FOUND);
        List<CouponCalcItem> applicable = scopeMatcher.matchItems(template, items);
        return couponOrderConverter.toCalcCoupon(uc, applicable);
    }

    /**
     * 填充候选券与推荐组合。
     *
     * @param resp 响应
     * @param items 商品明细
     * @param userId 用户 ID
     */
    private void fillCandidates(CouponOrderCalcResp resp, List<CouponCalcItem> items, Long userId) {
        List<UserCouponDO> available = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.AVAILABLE)
        );
        List<CouponCandidateResp> candidates = new ArrayList<>();
        List<CalcCoupon> validPlatform = new ArrayList<>();
        Map<Long, List<CalcCoupon>> validShopByShop = new LinkedHashMap<>();
        for (UserCouponDO uc : available) {
            if (uc.isOverdue()) {
                continue;
            }
            CalcCoupon calcCoupon = toCalcCoupon(uc, items);
            BigDecimal applicableAmount = sumLineAmount(calcCoupon.getApplicableItems());
            boolean valid = calcCoupon.isUsable();
            candidates.add(toCandidate(uc, calcCoupon, applicableAmount, valid));
            if (!valid) {
                continue;
            }
            if (uc.isPlatform()) {
                validPlatform.add(calcCoupon);
            } else {
                validShopByShop.computeIfAbsent(uc.getShopId(), k -> new ArrayList<>())
                               .add(calcCoupon);
            }
        }
        resp.setCandidates(candidates);
        resp.setRecommendedUserCouponIds(recommend(validPlatform, validShopByShop, items));
    }

    /**
     * 组装候选券响应。
     *
     * @param uc 用户券
     * @param calcCoupon 计算券
     * @param applicableAmount 适用金额
     * @param valid 是否可用
     * @return 候选券
     */
    private CouponCandidateResp toCandidate(
            UserCouponDO uc,
            CalcCoupon calcCoupon,
            BigDecimal applicableAmount,
            boolean valid
    ) {
        String invalidReason = null;
        if (!valid) {
            invalidReason = CollUtil.isEmpty(calcCoupon.getApplicableItems())
                    ? "商品不在适用范围内"
                    : "未满足使用门槛";
        }
        return CouponCandidateResp
                .builder()
                .userCouponId(uc.getId())
                .couponId(uc.getCouponId())
                .ownerType(uc.getOwnerType())
                .shopId(uc.getShopId())
                .name(uc.getNameSnapshot())
                .type(uc.getTypeSnapshot())
                .scopeType(uc.getScopeTypeSnapshot())
                .threshold(uc.getThresholdSnapshot())
                .discountAmount(uc.getDiscountAmountSnapshot())
                .discountRate(uc.getDiscountRateSnapshot())
                .effectiveTime(uc.getEffectiveTime())
                .expireTime(uc.getExpireTime())
                .applicableAmount(applicableAmount)
                .estimatedDiscountAmount(
                        valid ? estimateDiscount(calcCoupon, applicableAmount) : null
                )
                .valid(valid)
                .invalidReason(invalidReason)
                .build();
    }

    /**
     * 推荐最大优惠组合：枚举合法候选券组合，取商品应付金额最小， 相同优惠时优先更早过期的组合。
     *
     * @param validPlatform 合法平台券
     * @param validShopByShop 各店铺合法店铺券
     * @param items 商品明细
     * @return 推荐 userCouponId 列表（可空表示不使用券）
     */
    private List<Long> recommend(
            List<CalcCoupon> validPlatform,
            Map<Long, List<CalcCoupon>> validShopByShop,
            List<CouponCalcItem> items
    ) {
        List<List<CalcCoupon>> layers = new ArrayList<>();
        layers.add(withNullOption(validPlatform));
        for (List<CalcCoupon> shopCandidates : validShopByShop.values()) {
            layers.add(withNullOption(shopCandidates));
        }
        BigDecimal bestPayable = sumLineAmount(items);
        List<Long> best = Collections.emptyList();
        LocalDateTime bestEarliestExpire = null;
        for (List<CalcCoupon> combo : cartesian(layers)) {
            CalcCoupon platform = combo.isEmpty() ? null : combo.get(0);
            List<CalcCoupon> shops = combo.size() > 1
                    ? combo.subList(1, combo.size())
                    : Collections.emptyList();
            CouponOrderCalcResp candidateResp;
            try {
                candidateResp = calculator.calculate(items, platform, shops);
            } catch (BizException e) {
                continue;
            }
            BigDecimal payable = candidateResp.getGoodsPayableAmount();
            if (payable.compareTo(bestPayable) < 0) {
                bestPayable = payable;
                best = combo.stream()
                            .filter(Objects::nonNull)
                            .map(CalcCoupon::getUserCouponId)
                            .toList();
                bestEarliestExpire = earliestExpire(combo);
            } else if (payable.compareTo(bestPayable) == 0 && isEarlier(combo, bestEarliestExpire)) {
                best = combo.stream()
                            .filter(Objects::nonNull)
                            .map(CalcCoupon::getUserCouponId)
                            .toList();
                bestEarliestExpire = earliestExpire(combo);
            }
        }
        return best;
    }

    /**
     * 组合中加入空选项（表示不使用该层券）。
     *
     * @param coupons 候选券
     * @return 含空选项的列表
     */
    private List<CalcCoupon> withNullOption(List<CalcCoupon> coupons) {
        List<CalcCoupon> options = new ArrayList<>();
        options.add(null);
        options.addAll(coupons);
        return options;
    }

    /**
     * 笛卡尔积展开各层选项。
     *
     * @param layers 各层选项
     * @return 组合列表
     */
    private List<List<CalcCoupon>> cartesian(List<List<CalcCoupon>> layers) {
        List<List<CalcCoupon>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (List<CalcCoupon> layer : layers) {
            List<List<CalcCoupon>> next = new ArrayList<>();
            for (List<CalcCoupon> prefix : result) {
                for (CalcCoupon option : layer) {
                    List<CalcCoupon> copy = new ArrayList<>(prefix);
                    copy.add(option);
                    next.add(copy);
                }
            }
            result = next;
        }
        return result;
    }

    /**
     * 组合最早过期时间。
     *
     * @param combo 组合
     * @return 最早过期时间（无券为 null）
     */
    private LocalDateTime earliestExpire(List<CalcCoupon> combo) {
        LocalDateTime earliest = null;
        for (CalcCoupon coupon : combo) {
            if (coupon == null || coupon.getExpireTime() == null) {
                continue;
            }
            if (earliest == null || coupon.getExpireTime().isBefore(earliest)) {
                earliest = coupon.getExpireTime();
            }
        }
        return earliest;
    }

    /**
     * 同优惠时优先更早过期的组合。
     *
     * @param combo 候选组合
     * @param currentEarliest 当前最早过期时间
     * @return true 表示候选组合更优
     */
    private boolean isEarlier(List<CalcCoupon> combo, LocalDateTime currentEarliest) {
        LocalDateTime earliest = earliestExpire(combo);
        if (earliest == null && currentEarliest == null) {
            return false;
        }
        if (earliest == null) {
            return false;
        }
        if (currentEarliest == null) {
            return true;
        }
        return earliest.isBefore(currentEarliest);
    }

    /**
     * 锁定用户选择的券。
     *
     * @param req 锁定请求
     * @param resp 优惠快照
     */
    private void lockSelected(CouponOrderLockReq req, CouponOrderCalcResp resp) {
        for (CouponUsageResult usage : resp.getUsages()) {
            userCouponService.lock(
                    req.getOrderNo(),
                    req.getUserId(),
                    usage.getUserCouponId(),
                    req.getLockExpireTime()
            );
        }
    }

    /**
     * 判断订单是否已锁定过用户券（流水幂等）。
     *
     * @param orderNo 订单号
     * @return true 表示已锁定
     */
    private boolean hasLocked(String orderNo) {
        Long count = userCouponLogMapper.selectCount(
                Wrappers.<UserCouponLogDO>lambdaQuery()
                        .eq(UserCouponLogDO::getEventId, orderNo + ":LOCK")
                        .eq(UserCouponLogDO::getOperationType, UserCouponOperationTypeEnum.LOCK)
        );
        return count != null && count > 0;
    }

    /**
     * 幂等重放：按订单已锁定的用户券重建优惠快照。
     *
     * @param req 锁定请求
     * @return 优惠快照
     */
    private CouponOrderCalcResp rebuildByOrder(CouponOrderLockReq req) {
        List<UserCouponDO> locked = userCouponMapper.selectList(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, req.getUserId())
                        .eq(UserCouponDO::getLockedOrderNo, req.getOrderNo())
                        .eq(UserCouponDO::getStatus, UserCouponStatusEnum.LOCKED)
        );
        AssertUtils.notEmpty(locked, BizErrorCode.COUPON_LOCK_FAILED);
        CalcCoupon platform = null;
        List<CalcCoupon> shops = new ArrayList<>();
        for (UserCouponDO uc : locked) {
            CalcCoupon calcCoupon = toCalcCoupon(uc, req.getItems());
            if (uc.isPlatform()) {
                platform = calcCoupon;
            } else {
                shops.add(calcCoupon);
            }
        }
        return calculator.calculate(req.getItems(), platform, shops);
    }

    /**
     * 累加明细行金额。
     *
     * @param items 明细列表
     * @return 合计
     */
    private BigDecimal sumLineAmount(List<CouponCalcItem> items) {
        if (CollUtil.isEmpty(items)) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                    .map(CouponCalcItem::getLineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 估算优惠金额（候选展示用，与计算器公式一致）。
     *
     * @param coupon 计算券
     * @param applicableAmount 适用金额
     * @return 优惠金额
     */
    private BigDecimal estimateDiscount(CalcCoupon coupon, BigDecimal applicableAmount) {
        if (coupon.isDiscount()) {
            return applicableAmount.multiply(coupon.getDiscountRate())
                                   .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        BigDecimal amount = coupon.getDiscountAmount();
        return amount.compareTo(applicableAmount) > 0 ? applicableAmount : amount;
    }
}
