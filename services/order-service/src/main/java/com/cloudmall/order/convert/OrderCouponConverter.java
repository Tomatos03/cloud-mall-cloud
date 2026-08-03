package com.cloudmall.order.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.response.CouponUsageResult;
import com.cloudmall.order.api.response.OrderCouponResp;
import com.cloudmall.order.entity.OrderCouponDO;

/**
 * 订单优惠关联转换器
 * <p>
 * 将优惠试算/锁定返回的券快照落库为订单优惠关联快照，
 * 并支持将关联快照转换为查询响应。
 * 平台券关联父订单，店铺券关联对应店铺子订单。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface OrderCouponConverter {

    /**
     * 券快照转订单优惠关联。
     *
     * @param usage 券快照
     * @param parentOrderId 父订单 ID
     * @param subOrderId 子订单 ID（平台券为 null）
     * @return 订单优惠关联实体
     */
    @Mapping(target = "typeSnapshot", source = "usage.type")
    OrderCouponDO toCouponDO(CouponUsageResult usage, Long parentOrderId, Long subOrderId);

    /**
     * 订单优惠关联转响应。
     *
     * @param coupon 订单优惠关联实体
     * @return 订单优惠关联响应
     */
    @Mapping(target = "ownerType", source = "ownerType.code")
    @Mapping(target = "typeSnapshot", source = "typeSnapshot.code")
    OrderCouponResp toResp(OrderCouponDO coupon);

    /**
     * 批量转换订单优惠关联实体列表为响应列表。
     *
     * @param coupons 订单优惠关联实体列表
     * @return 订单优惠关联响应列表
     */
    List<OrderCouponResp> toRespList(List<OrderCouponDO> coupons);
}
