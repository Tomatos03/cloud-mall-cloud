package com.cloudmall.coupon.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.model.bo.CouponCalcItem;
import com.cloudmall.coupon.api.model.req.CouponOrderCommitByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderCommitReq;
import com.cloudmall.coupon.api.model.req.CouponOrderLockReq;
import com.cloudmall.coupon.api.model.req.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertReq;
import com.cloudmall.coupon.api.model.resp.CouponCandidateResp;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.coupon.api.model.resp.CouponUsageResult;
import com.cloudmall.coupon.calc.CalcCoupon;
import com.cloudmall.coupon.calc.CouponCalculator;
import com.cloudmall.coupon.calc.CouponScopeMatcher;
import com.cloudmall.coupon.convert.CouponOrderConverter;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.model.entity.CouponDO;
import com.cloudmall.coupon.model.entity.UserCouponDO;
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
    @Transactional(rollbackFor = Exception.class)
    public CouponOrderCalcResp lock(CouponOrderLockReq req) {
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
    @Transactional(rollbackFor = Exception.class)
    public void commit(CouponOrderCommitReq req) {
        userCouponService.commit(req.getOrderNo(), req.getUserId(), req.getUserCouponId());
    }

    /**
     * 取消/超时释放。
     *
     * @param req 释放请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(CouponOrderReleaseReq req) {
        userCouponService.release(
                req.getOrderNo(),
                req.getUserId(),
                req.getUserCouponId(),
                Objects.requireNonNullElse(req.getReason(), DEFAULT_RELEASE_REASON)
        );
    }

    /**
     * 按订单释放全部已锁定用户券（取消/超时/补偿场景，幂等）。
     *
     * @param req 释放请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseByOrder(CouponOrderReleaseByOrderReq req) {
        List<UserCouponDO> locked = listLockedByOrder(req.getOrderNo());
        for (UserCouponDO uc : locked) {
            userCouponService.release(req.getOrderNo(), uc.getUserId(), uc.getId(), DEFAULT_RELEASE_REASON);
        }
    }

    /**
     * 按订单核销全部已锁定用户券（支付成功事件，订单号幂等）。
     *
     * @param req 核销请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void commitByOrder(CouponOrderCommitByOrderReq req) {
        List<UserCouponDO> locked = listLockedByOrder(req.getOrderNo());
        for (UserCouponDO uc : locked) {
            userCouponService.commit(req.getOrderNo(), uc.getUserId(), uc.getId());
        }
    }

    /**
     * 按订单回退全部已核销用户券（全额退款事件，订单号 + 退款单号幂等）。
     *
     * @param req 回退请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertByOrder(CouponOrderRevertByOrderReq req) {
        List<UserCouponDO> used = userCouponMapper.selectUsedByOrder(req.getOrderNo());
        for (UserCouponDO uc : used) {
            userCouponService.revert(
                    req.getOrderNo(),
                    uc.getUserId(),
                    uc.getId(),
                    req.getRefundId(),
                    DEFAULT_REVERT_REASON
            );
        }
    }

    /**
     * 按订单号查询全部已锁定用户券。
     *
     * @param orderNo 订单号
     * @return 已锁定用户券列表
     */
    private List<UserCouponDO> listLockedByOrder(String orderNo) {
        return userCouponMapper.selectLockedByOrder(orderNo);
    }

    /**
     * 全额退款回退。
     *
     * @param req 回退请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        Asserts.notEmpty(items, BizErrorEnum.INVALID_PARAM);
        Long platformId = resolvePlatformCouponId(req);
        Map<Long, Long> shopIds = resolveShopCouponIds(req);
        CalcCoupon platformCoupon = resolveSelectedPlatform(req.getUserId(), platformId, items);
        List<CalcCoupon> shopCoupons = resolveSelectedShops(req.getUserId(), shopIds, items);
        return calculator.calculate(items, platformCoupon, shopCoupons);
    }

    /**
     * 从 userCouponIds 中解析平台券 userCouponId。
     *
     * @param req 试算请求
     * @return 平台券 userCouponId（未选择为 null）
     */
    private Long resolvePlatformCouponId(CouponOrderQuoteReq req) {
        if (CollUtil.isEmpty(req.getUserCouponIds())) {
            return null;
        }
        List<UserCouponDO> userCoupons = userCouponMapper.selectByIds(req.getUserCouponIds());
        return userCoupons.stream()
                          .filter(UserCouponDO::isPlatform)
                          .map(UserCouponDO::getId)
                          .findFirst()
                          .orElse(null);
    }

    /**
     * 从 userCouponIds 中解析店铺券（shopId → userCouponId）。
     *
     * @param req 试算请求
     * @return 店铺券 map（未选择为空 map）
     */
    private Map<Long, Long> resolveShopCouponIds(CouponOrderQuoteReq req) {
        if (CollUtil.isEmpty(req.getUserCouponIds())) {
            return Collections.emptyMap();
        }
        List<UserCouponDO> userCoupons = userCouponMapper.selectByIds(req.getUserCouponIds());
        Map<Long, Long> result = new LinkedHashMap<>();
        for (UserCouponDO uc : userCoupons) {
            if (uc.isShopCoupon()) {
                result.put(uc.getShopId(), uc.getId());
            }
        }
        return result;
    }

    /**
     * 解析用户选择的平台券。
     *
     * @param userId 用户 ID
     * @param userCouponId 用户券 ID（可空）
     * @param items 商品明细
     * @return 计算券（未选择为 null）
     */
    private CalcCoupon resolveSelectedPlatform(Long userId, Long userCouponId, List<CouponCalcItem> items) {
        if (userCouponId == null) {
            return null;
        }
        UserCouponDO uc = requireOwnedAvailable(userId, userCouponId);
        Asserts.isTrue(uc.isPlatform(), BizErrorEnum.COUPON_OWNER_INVALID);
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
            UserCouponDO uc = requireOwnedAvailable(userId, ucId);
            Asserts.isTrue(uc.isShopCoupon(), BizErrorEnum.COUPON_OWNER_INVALID);
            Asserts.isEqual(uc.getShopId(), entry.getKey(), BizErrorEnum.COUPON_OWNER_INVALID);
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
    private UserCouponDO requireOwnedAvailable(Long userId, Long userCouponId) {
        UserCouponDO uc = Asserts.requireNonNull(
                userCouponMapper.selectById(userCouponId),
                BizErrorEnum.USER_COUPON_NOT_FOUND
        );
        Asserts.isEqual(uc.getUserId(), userId, BizErrorEnum.USER_COUPON_NOT_BELONG_TO_USER);
        Asserts.isFalse(uc.isOverdue(), BizErrorEnum.USER_COUPON_EXPIRED);
        Asserts.isFalse(uc.isLocked(), BizErrorEnum.USER_COUPON_LOCKED);
        Asserts.isFalse(uc.isUsed(), BizErrorEnum.USER_COUPON_USED);
        Asserts.isTrue(uc.isAvailable(), BizErrorEnum.USER_COUPON_STATE_CONFLICT);
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
        CouponDO template = Asserts.requireNonNull(
                couponMapper.selectById(uc.getCouponId()),
                BizErrorEnum.COUPON_TEMPLATE_NOT_FOUND
        );
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
        List<UserCouponDO> available = userCouponMapper.selectAvailableByUser(userId);
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
                validShopByShop.computeIfAbsent(uc.getShopId(), k -> new ArrayList<>()).add(calcCoupon);
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
            invalidReason = CollUtil.isEmpty(calcCoupon.getApplicableItems()) ? "商品不在适用范围内" : "未满足使用门槛";
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
                .estimatedDiscountAmount(valid ? estimateDiscount(calcCoupon, applicableAmount) : null)
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
        List<List<CalcCoupon>> layers = buildLayers(validPlatform, validShopByShop);
        BigDecimal bestPayable = sumLineAmount(items);
        List<Long> best = Collections.emptyList();
        LocalDateTime bestEarliestExpire = null;
        for (List<CalcCoupon> combo : cartesian(layers)) {
            CalcCoupon platform = combo.isEmpty() ? null : combo.get(0);
            List<CalcCoupon> shops = combo.size() > 1
                    ? combo.subList(1, combo.size())
                           .stream()
                           .filter(Objects::nonNull)
                           .toList()
                    : Collections.emptyList();
            CouponOrderCalcResp candidateResp;
            candidateResp = calculator.calculate(items, platform, shops);
            BigDecimal payable = candidateResp.getProductPayableAmount();
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
     * 构造推荐组合层级：每一层代表一个决策点（第 0 层为平台券，之后每层为一家店铺的店铺券）， 层内首元
     * 素为 null，表示"该层不使用优惠券"。 <p>
     * 空选项是必要的：推荐必须枚举"不用券"的可能——例如某层没有合法券、 或组合了其他券后本层用券不再划
     * 算时，应退回到不用券； 且全 null 组合（不使用任何券）恰是原价基准， 用于与所有用券组合比较得
     * 出最低应付金额。
     *
     * @param validPlatform 合法平台券列表
     * @param validShopByShop 各店铺合法店铺券映射
     * @return 推荐组合层级，每层首元素为 null（表示该层不使用券）
     */
    private List<List<CalcCoupon>> buildLayers(
        List<CalcCoupon> validPlatform,
        Map<Long, List<CalcCoupon>> validShopByShop
    ) {
        List<List<CalcCoupon>> layers = new ArrayList<>();
        layers.add(withNullOption(validPlatform));
        for (List<CalcCoupon> shopCandidates : validShopByShop.values()) {
            layers.add(withNullOption(shopCandidates));
        }
        return layers;
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
     * 笛卡尔积展开各层选项。 <p>
     * 使用逐层累积迭代法：从空组合出发，每加入一层，将已有组合与当前层 的每个选项逐一配对，生成更大的组合
     * 集合；经过所有层后得到完整的 笛卡尔积。组合数 = ∏(各层选项数)。
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
            if (coupon == null || !coupon.hasExpireTime()) {
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
        Long count = userCouponMapper.countLockedByOrder(orderNo);
        return count != null && count > 0;
    }

    /**
     * 幂等重放：按订单已锁定的用户券重建优惠快照。
     *
     * @param req 锁定请求
     * @return 优惠快照
     */
    private CouponOrderCalcResp rebuildByOrder(CouponOrderLockReq req) {
        List<UserCouponDO> locked = userCouponMapper.selectLockedByUserAndOrder(
                req.getUserId(),
                req.getOrderNo()
        );
        Asserts.notEmpty(locked, BizErrorEnum.COUPON_LOCK_FAILED);
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
        return coupon.getType()
                     .calcDiscount(
                             applicableAmount,
                             coupon.getDiscountAmount(),
                             coupon.getDiscountRate(),
                             2,
                             RoundingMode.HALF_UP
                     );
    }
}
