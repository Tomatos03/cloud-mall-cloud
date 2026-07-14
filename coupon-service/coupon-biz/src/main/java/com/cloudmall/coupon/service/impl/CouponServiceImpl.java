package com.cloudmall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.coupon.api.request.CouponClaimRequest;
import com.cloudmall.coupon.api.response.CouponResponse;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<CouponResponse> listAvailable() {
        List<CouponDO> list = couponMapper.selectList(
                new LambdaQueryWrapper<CouponDO>()
                        .eq(CouponDO::getStatus, 1)
                        .gt(CouponDO::getExpireTime, LocalDateTime.now())
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public CouponResponse getById(Long id) {
        CouponDO coupon = couponMapper.selectById(id);
        if (coupon == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        return toResponse(coupon);
    }

    @Override
    @Transactional
    public Boolean claim(CouponClaimRequest request) {
        CouponDO coupon = couponMapper.selectById(request.getCouponId());
        if (coupon == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        if (coupon.getStatus() != 1) throw new BizException(BizErrorCode.COUPON_NOT_AVAILABLE);
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now()))
            throw new BizException(BizErrorCode.COUPON_EXPIRED);
        if (coupon.getTotalCount() != null && coupon.getClaimedCount() >= coupon.getTotalCount())
            throw new BizException(BizErrorCode.COUPON_STOCK_RUN_OUT);

        // Check already claimed
        Long count = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCouponDO>()
                        .eq(UserCouponDO::getUserId, request.getUserId())
                        .eq(UserCouponDO::getCouponId, request.getCouponId())
        );
        if (count > 0) throw new BizException(BizErrorCode.COUPON_ALREADY_CLAIMED);

        UserCouponDO uc = new UserCouponDO();
        uc.setUserId(request.getUserId());
        uc.setCouponId(request.getCouponId());
        uc.setStatus("UNUSED");
        uc.setClaimedTime(LocalDateTime.now());
        userCouponMapper.insert(uc);

        coupon.setClaimedCount(coupon.getClaimedCount() == null ? 1 : coupon.getClaimedCount() + 1);
        couponMapper.updateById(coupon);
        return true;
    }

    @Override
    public CouponResponse verifyCoupon(Long couponId, Long userId) {
        UserCouponDO uc = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCouponDO>()
                        .eq(UserCouponDO::getUserId, userId)
                        .eq(UserCouponDO::getCouponId, couponId)
                        .eq(UserCouponDO::getStatus, "UNUSED")
        );
        if (uc == null) throw new BizException(BizErrorCode.COUPON_NOT_AVAILABLE);
        return getById(couponId);
    }

    @Override
    @Transactional
    public void markUsed(Long id) {
        UserCouponDO uc = userCouponMapper.selectById(id);
        if (uc == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        uc.setStatus("USED");
        uc.setUsedTime(LocalDateTime.now());
        userCouponMapper.updateById(uc);
    }

    private CouponResponse toResponse(CouponDO coupon) {
        CouponResponse r = new CouponResponse();
        r.setId(coupon.getId());
        r.setName(coupon.getName());
        r.setType(coupon.getType());
        r.setThreshold(coupon.getThreshold());
        r.setDiscount(coupon.getDiscount());
        r.setExpireTime(coupon.getExpireTime());
        return r;
    }
}
