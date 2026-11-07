package com.cloudmall.coupon.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.coupon.model.entity.UserCouponDO;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.model.PageReq;

/**
 * 我的优惠券分页查询请求
 * <p>
 * 用户端不接收 userId，统一从当前用户上下文获取；status 为空时查询全部状态。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponPageReq extends PageReq<UserCouponDO> {

    /** 用户券状态过滤（null 表示全部） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private UserCouponStatusEnum status;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public UserCouponPageReq(long current, long size) {
        super(current, size);
    }
}
