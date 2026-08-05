package com.cloudmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.coupon.api.response.CouponOrderCalcResp;
import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.enums.ParentOrderStatusEnum;
import com.cloudmall.order.model.req.OrderPageReq;

/**
 * 订单 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    /**
     * 分页查询父订单，按创建时间倒序。 <p>
     * 条件由 {@link OrderPageReq} 的 {@code @QueryCondition}
     * 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 父订单分页数据
     */
    default IPage<OrderDO> selectOrderPage(OrderPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 标记父订单为已取消（幂等，仅创建中/待支付可取消）。
     *
     * @param orderId 父订单 ID
     * @param status 目标状态
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_order
            SET status = #{status}
            WHERE id = #{orderId}
              AND status IN ('CREATING', 'PENDING_PAYMENT')
            """
    )
    int markCancelled(
            @Param("orderId")
            Long orderId,
            @Param("status")
            ParentOrderStatusEnum status
    );

    /**
     * 更新父订单金额。
     *
     * @param orderId 父订单 ID
     * @param calc 优惠计算/锁定结果
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_order
            SET total_original_amount = #{calc.totalOriginalAmount},
                total_shop_discount_amount = #{calc.totalShopDiscountAmount},
                total_platform_discount_amount = #{calc.totalPlatformDiscountAmount},
                goods_payable_amount = #{calc.goodsPayableAmount},
                payable_amount = #{calc.payableAmount}
            WHERE id = #{orderId}
            """
    )
    int updateParentAmounts(
            @Param("orderId")
            Long orderId,
            @Param("calc")
            CouponOrderCalcResp calc
    );

    /**
     * 父订单迁移为待支付（条件更新防并发）。
     *
     * @param orderId 父订单 ID
     * @param status 目标状态
     * @return 影响行数，0 表示状态已变更或订单不存在
     */
    @Update(
            """
            UPDATE cm_order
            SET status = #{status}
            WHERE id = #{orderId}
              AND status = 'CREATING'
            """
    )
    int transitionToPendingPayment(
            @Param("orderId")
            Long orderId,
            @Param("status")
            ParentOrderStatusEnum status
    );
}
