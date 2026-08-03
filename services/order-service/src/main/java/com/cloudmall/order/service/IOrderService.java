package com.cloudmall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.order.api.request.CreateOrderReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.api.response.OrderSubResp;
import com.cloudmall.order.model.req.OrderPageReq;

/**
 * 订单服务
 * <p>
 * 负责下单 Saga 编排（校验 → CREATING 骨架 → 预占库存 → 锁券 →
 * 落库优惠/明细 → 待支付 → 删购物车），失败按逆序补偿。
 *
 * @author : Tomatos
 */
public interface IOrderService {

    /**
     * 创建订单（跨店结算 Saga）。
     *
     * @param req 下单请求
     * @return 父订单详情
     */
    OrderResp createOrder(CreateOrderReq req);

    /**
     * 获取父订单详情（仅当前用户本人可查）。
     *
     * @param id 父订单 ID
     * @return 父订单详情
     */
    OrderResp getById(Long id);

    /**
     * 用户分页查询自己的父订单。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    IPage<OrderResp> pageUser(OrderPageReq req);

    /**
     * 商家分页查询自己店铺的子订单。
     *
     * @param current 当前页码
     * @param size 页大小
     * @return 子订单分页
     */
    IPage<OrderSubResp> pageMerchant(long current, long size);

    /**
     * 运营分页查询全量父订单。
     *
     * @param req 分页请求
     * @return 父订单分页
     */
    IPage<OrderResp> pageOperator(OrderPageReq req);
}
