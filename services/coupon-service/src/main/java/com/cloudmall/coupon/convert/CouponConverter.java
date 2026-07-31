package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.entity.CouponDO;

@Mapper(componentModel = "spring")
public interface CouponConverter {

    @Mapping(target = "claimed", ignore = true)
    CouponResp toResp(CouponDO coupon);

    /**
     * 批量转换优惠券实体列表为响应列表。
     *
     * @param coupons 优惠券实体列表
     * @return 优惠券响应列表
     */
    List<CouponResp> toRespList(List<CouponDO> coupons);
}
