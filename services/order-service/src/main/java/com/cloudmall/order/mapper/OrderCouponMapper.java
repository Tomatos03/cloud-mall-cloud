package com.cloudmall.order.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cloudmall.order.entity.OrderCouponDO;

/**
 * 订单优惠关联 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderCouponMapper extends BaseMapper<OrderCouponDO> {

    /**
     * 查询父订单下的全部优惠关联快照。
     *
     * @param parentOrderId 父订单 ID
     * @return 优惠关联列表
     */
    @Select(
            """
            SELECT *
            FROM cm_order_coupon
            WHERE parent_order_id = #{parentOrderId} AND deleted = 0
            ORDER BY id
            """
    )
    List<OrderCouponDO> selectByParentOrderId(
            @Param("parentOrderId")
            Long parentOrderId
    );

    /**
     * 聚合指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return order_count 与 subsidy_amount
     */
    @Select(
            """
            SELECT COUNT(DISTINCT parent_order_id) AS order_count,
                   COALESCE(SUM(discount_amount), 0) AS subsidy_amount
            FROM cm_order_coupon
            WHERE coupon_id = #{couponId} AND deleted = 0
            """
    )
    Map<String, Object> aggregateByCoupon(
            @Param("couponId")
            Long couponId
    );

    /**
     * 聚合全部模板的使用订单数与补贴金额。
     *
     * @return order_count 与 subsidy_amount
     */
    @Select(
            """
            SELECT COUNT(DISTINCT parent_order_id) AS order_count,
                   COALESCE(SUM(discount_amount), 0) AS subsidy_amount
            FROM cm_order_coupon
            WHERE deleted = 0
            """
    )
    Map<String, Object> aggregateOverview();
}
