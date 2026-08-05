package com.cloudmall.order.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.api.response.CouponSubOrderResult;
import com.cloudmall.order.entity.OrderSubDO;
import com.cloudmall.order.enums.SubOrderStatusEnum;
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
    default List<OrderSubDO> selectByParentOrderId(Long parentOrderId) {
        return selectList(
                Wrappers.<OrderSubDO>lambdaQuery()
                        .eq(OrderSubDO::getParentOrderId, parentOrderId)
                        .orderByAsc(OrderSubDO::getId)
        );
    }

    /**
     * 更新子订单金额。
     *
     * @param subOrderId 子订单 ID
     * @param result 子订单优惠结果
     * @return 影响行数
     */
    default int updateSubAmounts(Long subOrderId, CouponSubOrderResult result) {
        return update(
                Wrappers.<OrderSubDO>lambdaUpdate()
                        .set(OrderSubDO::getOriginalAmount, result.getOriginalAmount())
                        .set(OrderSubDO::getShopDiscountAmount, result.getShopDiscountAmount())
                        .set(OrderSubDO::getPlatformDiscountAmount, result.getPlatformDiscountAmount())
                        .set(OrderSubDO::getPayableAmount, result.getPayableAmount())
                        .set(OrderSubDO::getMerchantSettlementAmount, result.getPayableAmount())
                        .eq(OrderSubDO::getId, subOrderId)
        );
    }

    /**
     * 校验并迁移子订单状态（带迁移前状态条件，防止并发覆盖）。
     *
     * @param subOrderId 子订单 ID
     * @param from 迁移前状态
     * @param to 迁移后状态
     * @return 影响行数，0 表示状态已变更或订单不存在
     */
    default int transitionStatus(Long subOrderId, SubOrderStatusEnum from, SubOrderStatusEnum to) {
        return update(
                Wrappers.<OrderSubDO>lambdaUpdate()
                        .set(OrderSubDO::getStatus, to)
                        .eq(OrderSubDO::getId, subOrderId)
                        .eq(OrderSubDO::getStatus, from)
        );
    }
}
