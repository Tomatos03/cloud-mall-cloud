package com.cloudmall.coupon.model.req;

import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.coupon.enums.UserCouponUsableEnum;

/**
 * 我的优惠券游标分页查询请求
 * <p>
 * 用户端不接收 userId，统一从当前用户上下文获取。
 * status 为空表示查询全部状态；status 为 AVAILABLE/UNAVAILABLE 时表示查询
 * 可用/不可用分组，此时必须携带 items（当前购物车 SKU 与数量），由服务端
 * 使用可信商品数据判定范围与门槛。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponCursorReq extends CursorReq {

    /** 可用性分组（AVAILABLE 可用 / UNAVAILABLE 不可用，空表示全部） */
    private UserCouponUsableEnum status;

    /** 评估上下文：当前购物车 SKU 与数量（status 非空时必须传） */
    @Valid
    private List<CouponCursorItemReq> items;
}
