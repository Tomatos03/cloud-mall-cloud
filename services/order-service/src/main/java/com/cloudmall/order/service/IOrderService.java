package com.cloudmall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.order.api.model.resp.CouponUsageStatsResp;
import com.cloudmall.order.api.model.resp.OrderResp;
import com.cloudmall.order.api.model.resp.OrderSubResp;
import com.cloudmall.order.model.req.OrderPageReq;
import com.cloudmall.order.model.req.OrderSubPageReq;

/**
 * 订单查询服务
 * <p>
 * 负责父/子订单的本地查询与响应组装，以及订单优惠使用统计；涉及远程调用的下单与预览流程由
 * {@link com.cloudmall.order.application.OrderApp} 编排。
 *
 * @author : Tomatos
 */
public interface IOrderService {

    /**
     * 获取父订单详情（仅当前用户本人可查）。
     *
     * @param id 父订单 ID
     * @return 父订单详情
     */
    OrderResp getById(Long id);

    /**
     * 分页查询父订单。 <p>
     * 查询条件由调用方组装：用户端点需注入当前用户 ID 限制本人订单， 运营端点可传全量条件或不带
     * userId 查询全部。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    IPage<OrderResp> page(OrderPageReq req);

    /**
     * 商家分页查询自己店铺的子订单。
     *
     * @param req 分页请求（shopId 由服务层注入）
     * @return 子订单分页
     */
    IPage<OrderSubResp> pageMerchant(OrderSubPageReq req);

    /**
     * 查询指定模板的使用订单数与补贴金额。
     *
     * @param couponId 模板 ID
     * @return 使用统计
     */
    CouponUsageStatsResp usageByCoupon(Long couponId);

    /**
     * 查询全局使用订单数与补贴金额。
     *
     * @return 使用统计
     */
    CouponUsageStatsResp usageOverview();
}
