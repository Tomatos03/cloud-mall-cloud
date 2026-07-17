package com.cloudmall.coupon.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.coupon.api.request.ClaimReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.convert.CouponConverter;
import com.cloudmall.coupon.service.ICouponService;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponConverter couponConverter;

    @Override
    public List<CouponResp> listAvailable() {
        List<CouponDO> list = couponMapper.selectList(
                Wrappers.<CouponDO>lambdaQuery()
                        .eq(CouponDO::getStatus, 1)
                        .gt(CouponDO::getExpireTime, LocalDateTime.now())
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public CouponResp getById(Long id) {
        CouponDO coupon = couponMapper.selectById(id);
        AssertUtils.notNull(coupon, BizErrorCode.DATA_NOT_FOUND);
        return toResponse(coupon);
    }

    @Override
    @Transactional
    public Boolean claim(ClaimReq request) {
        CouponDO coupon = couponMapper.selectById(request.getCouponId());
        AssertUtils.notNull(coupon, BizErrorCode.DATA_NOT_FOUND);
        AssertUtils.isTrue(coupon.getStatus() == 1, BizErrorCode.COUPON_NOT_AVAILABLE);
        AssertUtils.isFalse(coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now()), BizErrorCode.COUPON_EXPIRED);
        AssertUtils.isFalse(coupon.getTotalCount() != null && coupon.getClaimedCount() >= coupon.getTotalCount(), BizErrorCode.COUPON_STOCK_RUN_OUT);

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

    @Override
    @Transactional
    public void markUsed(Long id) {
        UserCouponDO uc = userCouponMapper.selectById(id);
        AssertUtils.notNull(uc, BizErrorCode.DATA_NOT_FOUND);
        uc.setStatus("USED");
        uc.setUsedTime(LocalDateTime.now());
        userCouponMapper.updateById(uc);
    }

    private CouponResp toResponse(CouponDO coupon) {
        return couponConverter.toResp(coupon);
    }
}
