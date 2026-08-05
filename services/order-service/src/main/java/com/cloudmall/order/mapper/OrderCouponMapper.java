package com.cloudmall.order.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

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
    default List<OrderCouponDO> selectByParentOrderId(Long parentOrderId) {
        return selectList(
                Wrappers.<OrderCouponDO>lambdaQuery()
                        .eq(OrderCouponDO::getParentOrderId, parentOrderId)
                        .orderByAsc(OrderCouponDO::getId)
        );
    }

    /**
     * 聚合指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return order_count 与 subsidy_amount
     */
    default Map<String, Object> aggregateByCoupon(Long couponId) {
        List<Map<String, Object>> rows = selectMaps(
                Wrappers.<OrderCouponDO>query()
                        .select(
                                "COUNT(DISTINCT parent_order_id) AS order_count",
                                "COALESCE(SUM(discount_amount), 0) AS subsidy_amount"
                        )
                        .eq("coupon_id", couponId)
                        .eq("deleted", 0)
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 聚合全部模板的使用订单数与补贴金额。
     *
     * @return order_count 与 subsidy_amount
     */
    default Map<String, Object> aggregateOverview() {
        List<Map<String, Object>> rows = selectMaps(
                Wrappers.<OrderCouponDO>query()
                        .select(
                                "COUNT(DISTINCT parent_order_id) AS order_count",
                                "COALESCE(SUM(discount_amount), 0) AS subsidy_amount"
                        )
                        .eq("deleted", 0)
        );
        return rows.isEmpty() ? null : rows.get(0);
    }
}
