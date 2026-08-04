package com.cloudmall.coupon.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.coupon.api.response.CouponStatsResp;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponLogDO;
import com.cloudmall.coupon.enums.UserCouponOperationTypeEnum;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponLogMapper;
import com.cloudmall.coupon.service.ICouponStatsService;
import com.cloudmall.order.api.client.OrderStatsClient;
import com.cloudmall.order.api.response.CouponUsageStatsResp;

/**
 * 优惠券统计服务实现
 * <p>
 * 模板维度按用户券流水操作类型聚合，成本维度经订单统计接口聚合。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CouponStatsService implements ICouponStatsService {

    private final CouponMapper couponMapper;
    private final UserCouponLogMapper userCouponLogMapper;
    private final OrderStatsClient orderStatsClient;

    /**
     * 统计指定模板的发行、核销与补贴。
     *
     * @param couponId 模板 ID
     * @return 模板统计
     */
    @Override
    public CouponStatsResp statsByCoupon(Long couponId) {
        CouponDO coupon = couponMapper.selectById(couponId);
        AssertUtils.notNull(coupon, BizErrorCode.COUPON_TEMPLATE_NOT_FOUND);
        requireManageable(coupon);
        CouponStatsResp stats = aggregate(couponId);
        stats.setCouponId(coupon.getId());
        stats.setName(coupon.getName());
        fillUsageStats(stats, orderStatsClient.usageByCoupon(couponId));
        return stats;
    }

    /**
     * 统计全平台发行、核销与补贴。
     *
     * @return 全局概览
     */
    @Override
    public CouponStatsResp overview() {
        AssertUtils.isTrue(UserContextHolder.isAdmin(), BizErrorCode.COUPON_OPERATION_NOT_ALLOWED);
        CouponStatsResp stats = aggregate(null);
        fillUsageStats(stats, orderStatsClient.usageOverview());
        return stats;
    }

    /**
     * 按操作类型聚合用户券流水计数。
     *
     * @param couponId 模板 ID（null 表示全量）
     * @return 统计响应（不含成本维度）
     */
    private CouponStatsResp aggregate(Long couponId) {
        List<UserCouponLogDO> logs = userCouponLogMapper.selectList(
                Wrappers.<UserCouponLogDO>lambdaQuery()
                        .eq(couponId != null, UserCouponLogDO::getCouponId, couponId)
        );
        CouponStatsResp stats = CouponStatsResp.builder()
                                               .issueCount(0L)
                                               .lockedCount(0L)
                                               .committedCount(0L)
                                               .releasedCount(0L)
                                               .revertedCount(0L)
                                               .expiredCount(0L)
                                               .revokedCount(0L)
                                               .orderCount(0L)
                                               .build();
        if (CollUtil.isEmpty(logs)) {
            return stats;
        }
        Map<UserCouponOperationTypeEnum, Long> countByOp = logs
                .stream()
                .map(UserCouponLogDO::getOperationType)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        stats.setIssueCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.ISSUE, 0L));
        stats.setLockedCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.LOCK, 0L));
        stats.setCommittedCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.COMMIT, 0L));
        stats.setReleasedCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.RELEASE, 0L));
        stats.setRevertedCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.REVERT, 0L));
        stats.setExpiredCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.EXPIRE, 0L));
        stats.setRevokedCount(countByOp.getOrDefault(UserCouponOperationTypeEnum.REVOKE, 0L));
        return stats;
    }

    /**
     * 填充成本维度（使用订单数与补贴金额）。
     *
     * @param stats 统计响应
     * @param usage 使用统计结果
     */
    private void fillUsageStats(CouponStatsResp stats, CouponUsageStatsResp usage) {
        if (usage == null) {
            return;
        }
        stats.setOrderCount(usage.getOrderCount());
        stats.setSubsidyAmount(usage.getSubsidyAmount());
    }

    /**
     * 校验当前用户可管理该模板：运营放行，非运营必须为店铺拥有者。
     *
     * @param coupon 模板
     */
    private void requireManageable(CouponDO coupon) {
        if (UserContextHolder.isAdmin()) {
            return;
        }
        Long shopId = UserContextHolder.getShopId();
        AssertUtils.notNull(shopId, BizErrorCode.MERCHANT_NO_SHOP);
        AssertUtils.isEqual(coupon.getShopId(), shopId, BizErrorCode.COUPON_OPERATION_NOT_ALLOWED);
    }
}
