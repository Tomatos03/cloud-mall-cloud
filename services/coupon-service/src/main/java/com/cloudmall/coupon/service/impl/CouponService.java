package com.cloudmall.coupon.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.convert.CouponConverter;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.service.ICouponService;
import com.cloudmall.mybatisplus.enums.StatusEnum;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponConverter couponConverter;

    /**
     * 查询数据列表。
     *
     * @return 数据列表
     */
    @Override
    public List<CouponResp> listAvailable() {
        List<CouponDO> list = couponMapper.selectList(
                Wrappers.<CouponDO>lambdaQuery()
                        .eq(CouponDO::getStatus, StatusEnum.ENABLED)
                        .gt(CouponDO::getExpireTime, LocalDateTime.now())
        );
        return couponConverter.toRespList(list);
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public CouponResp getById(Long id) {
        CouponDO coupon = couponMapper.selectById(id);
        AssertUtils.notNull(coupon, BizErrorCode.DATA_NOT_FOUND);
        return couponConverter.toResp(coupon);
    }

    /**
     * 领取优惠券。
     *
     * @param request 请求参数
     * @return 是否满足条件
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional
    public Boolean claim(ClaimReq request) {
        CouponDO coupon = couponMapper.selectById(request.getCouponId());
        AssertUtils.notNull(coupon, BizErrorCode.DATA_NOT_FOUND);
        AssertUtils.isTrue(coupon.getStatus() == StatusEnum.ENABLED, BizErrorCode.COUPON_NOT_AVAILABLE);
        AssertUtils.isFalse(
                coupon.getExpireTime() != null
                        && coupon.getExpireTime().isBefore(LocalDateTime.now()),
                BizErrorCode.COUPON_EXPIRED
        );
        AssertUtils.isFalse(
                coupon.getTotalCount() != null
                        && coupon.getClaimedCount() >= coupon.getTotalCount(),
                BizErrorCode.COUPON_STOCK_RUN_OUT
        );

        // Check already claimed
        Long count = userCouponMapper.selectCount(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, request.getUserId())
                        .eq(UserCouponDO::getCouponId, request.getCouponId())
        );
        AssertUtils.isZero(count, BizErrorCode.COUPON_ALREADY_CLAIMED);

        UserCouponDO uc = UserCouponDO.builder()
                .userId(request.getUserId())
                .couponId(request.getCouponId())
                .status("UNUSED")
                .claimedTime(LocalDateTime.now())
                .build();
        userCouponMapper.insert(uc);

        coupon.setClaimedCount(coupon.getClaimedCount() == null ? 1 : coupon.getClaimedCount() + 1);
        couponMapper.updateById(coupon);
        return true;
    }

    /**
     * 校验相关数据。
     *
     * @param couponId 优惠券标识
     * @param userId 用户标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public CouponResp verifyCoupon(Long couponId, Long userId) {
        UserCouponDO uc = userCouponMapper.selectOne(
                Wrappers.<UserCouponDO>lambdaQuery()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCouponId, couponId)
                        .eq(UserCouponDO::getStatus, "UNUSED")
        );
        AssertUtils.notNull(uc, BizErrorCode.COUPON_NOT_AVAILABLE);
        return getById(couponId);
    }

    /**
     * 标记优惠券为已使用。
     *
     * @param id 对象标识
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional
    public void markUsed(Long id) {
        UserCouponDO uc = userCouponMapper.selectById(id);
        AssertUtils.notNull(uc, BizErrorCode.DATA_NOT_FOUND);
        uc.setStatus("USED");
        uc.setUsedTime(LocalDateTime.now());
        userCouponMapper.updateById(uc);
    }
}
