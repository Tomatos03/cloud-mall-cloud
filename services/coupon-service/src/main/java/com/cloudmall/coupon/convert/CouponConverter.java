package com.cloudmall.coupon.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.entity.CouponDO;

@Mapper(componentModel = "spring")
public interface CouponConverter {

    @Mapping(target = "claimed", ignore = true)
    CouponResp toResp(CouponDO coupon);
}
