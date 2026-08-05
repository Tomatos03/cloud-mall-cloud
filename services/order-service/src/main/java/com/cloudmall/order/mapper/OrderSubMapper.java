package com.cloudmall.order.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.coupon.api.response.CouponSubOrderResult;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.model.req.OrderSubPageReq;

/**
 * 店铺子订单 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OrderSubMapper extends BaseMapper<OrderSubDO> {

    /**
     * 分页查询子订单，按创建时间倒序。 <p>
     * 条件由 {@link OrderSubPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 子订单分页数据
     */
    default IPage<OrderSubDO> selectMerchantPage(OrderSubPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 查询父订单下的全部子订单，按 ID 升序。
     *
     * @param parentOrderId 父订单 ID
     * @return 子订单列表
     */
    @Select(
            """
            SELECT *
            FROM cm_order_sub
            WHERE parent_order_id = #{parentOrderId} AND deleted = 0
            ORDER BY id
            """
    )
    List<OrderSubDO> selectByParentOrderId(
            @Param("parentOrderId")
            Long parentOrderId
    );

    /**
     * 更新子订单金额。
     *
     * @param subOrderId 子订单 ID
     * @param result 子订单优惠结果
     * @return 影响行数
     */
    @Update(
            """
            UPDATE cm_order_sub
            SET original_amount = #{result.originalAmount},
                shop_discount_amount = #{result.shopDiscountAmount},
                platform_discount_amount = #{result.platformDiscountAmount},
                payable_amount = #{result.payableAmount},
                merchant_settlement_amount = #{result.payableAmount}
            WHERE id = #{subOrderId}
            """
    )
    int updateSubAmounts(
            @Param("subOrderId")
            Long subOrderId,
            @Param("result")
            CouponSubOrderResult result
    );
}
