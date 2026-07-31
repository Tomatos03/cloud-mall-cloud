package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.entity.CouponDO;

/**
 * 优惠券模板转换器。
 * <p>
 * claimed/claimable/stockRemaining/productIds/categoryIds 由服务层按上下文补充。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CouponConverter {

    /**
     * 转换模板实体为响应对象。
     *
     * @param coupon 模板实体
     * @return 响应对象
     */
    CouponResp toResp(CouponDO coupon);

    /**
     * 批量转换模板实体列表为响应列表。
     *
     * @param coupons 模板实体列表
     * @return 响应列表
     */
    List<CouponResp> toRespList(List<CouponDO> coupons);
}
