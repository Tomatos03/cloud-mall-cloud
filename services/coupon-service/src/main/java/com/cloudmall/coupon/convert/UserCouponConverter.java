package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.coupon.api.response.UserCouponResp;
import com.cloudmall.coupon.entity.UserCouponDO;

/**
 * 用户券转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface UserCouponConverter {

    /**
     * 转换用户券实体为响应对象。
     *
     * @param userCoupon 用户券实体
     * @return 响应对象
     */
    UserCouponResp toResp(UserCouponDO userCoupon);

    /**
     * 批量转换用户券实体列表为响应列表。
     *
     * @param userCoupons 用户券实体列表
     * @return 响应列表
     */
    List<UserCouponResp> toRespList(List<UserCouponDO> userCoupons);
}
