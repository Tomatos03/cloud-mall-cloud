package com.cloudmall.order.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.order.model.entity.OrderDO;
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
     * 查询创建中超时父订单（按 ID 升序，供超时恢复任务扫描）。 <p>
     * 订单创建时间早于 {@code timeoutThreshold} 视为卡在 CREATING 状态
     * 。
     *
     * @param status 目标父订单状态
     * @param timeoutThreshold 创建时间阈值（早于该时间的视为超时）
     * @return 超时父订单列表
     */
    default List<OrderDO> selectStuckCreatingOrders(
        ParentOrderStatusEnum status,
        LocalDateTime timeoutThreshold
    ) {
        return selectList(
                Wrappers.<OrderDO>lambdaQuery()
                        .eq(OrderDO::getStatus, status)
                        .lt(OrderDO::getCreateTime, timeoutThreshold)
                        .orderByAsc(OrderDO::getId)
        );
    }

    /**
     * 标记父订单为已取消（幂等，仅创建中/待支付可取消）。
     *
     * @param orderId 父订单 ID
     * @param status 目标状态
     * @return 影响行数
     */
    default int markCancelled(Long orderId, ParentOrderStatusEnum status) {
        return update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, status)
                        .eq(OrderDO::getId, orderId)
                        .in(
                                OrderDO::getStatus,
                                ParentOrderStatusEnum.CREATING,
                                ParentOrderStatusEnum.PENDING_PAYMENT
                        )
        );
    }

    /**
     * 更新父订单金额。
     *
     * @param orderId 父订单 ID
     * @param calc 优惠计算/锁定结果
     * @return 影响行数
     */
    default int updateParentAmounts(Long orderId, CouponOrderCalcResp calc) {
        return update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getTotalOriginalAmount, calc.getTotalOriginalAmount())
                        .set(OrderDO::getTotalShopDiscountAmount, calc.getTotalShopDiscountAmount())
                        .set(OrderDO::getTotalPlatformDiscountAmount, calc.getTotalPlatformDiscountAmount())
                        .set(OrderDO::getProductPayableAmount, calc.getProductPayableAmount())
                        .set(OrderDO::getPayableAmount, calc.getProductPayableAmount())
                        .eq(OrderDO::getId, orderId)
        );
    }

    /**
     * 父订单迁移为待支付（条件更新防并发）。
     *
     * @param orderId 父订单 ID
     * @param status 目标状态
     * @return 影响行数，0 表示状态已变更或订单不存在
     */
    default int transitionToPendingPayment(Long orderId, ParentOrderStatusEnum status) {
        return update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, status)
                        .eq(OrderDO::getId, orderId)
                        .eq(OrderDO::getStatus, ParentOrderStatusEnum.CREATING)
        );
    }

    /**
     * 按订单号查询父订单。
     *
     * @param orderNo 父订单号
     * @return 父订单，不存在时返回 null
     */
    default OrderDO selectByOrderNo(String orderNo) {
        return selectOne(Wrappers.<OrderDO>lambdaQuery().eq(OrderDO::getOrderNo, orderNo));
    }

    /**
     * 校验并迁移父订单状态（带迁移前状态条件，防止并发覆盖）。
     *
     * @param orderId 父订单 ID
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return 影响行数，0 表示状态已变更或订单不存在
     */
    default int transitionStatus(Long orderId, ParentOrderStatusEnum from, ParentOrderStatusEnum to) {
        return update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, to)
                        .eq(OrderDO::getId, orderId)
                        .eq(OrderDO::getStatus, from)
        );
    }

    /**
     * 标记父订单为已全额退款（仅 PAID/PARTIAL_REFUNDED 可退款）。
     *
     * @param orderId 父订单 ID
     * @return 影响行数，0 表示状态不可退款或订单不存在
     */
    default int markRefunded(Long orderId) {
        return update(
                Wrappers.<OrderDO>lambdaUpdate()
                        .set(OrderDO::getStatus, ParentOrderStatusEnum.REFUNDED)
                        .eq(OrderDO::getId, orderId)
                        .in(
                                OrderDO::getStatus,
                                ParentOrderStatusEnum.PAID,
                                ParentOrderStatusEnum.PARTIAL_REFUNDED
                        )
        );
    }
}
