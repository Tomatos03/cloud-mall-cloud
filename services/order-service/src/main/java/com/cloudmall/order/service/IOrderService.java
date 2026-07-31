package com.cloudmall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.model.req.OrderPageReq;

public interface IOrderService {

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    OrderResp createOrder(CreateReq request);

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    OrderResp getById(Long id);

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    IPage<OrderResp> page(OrderPageReq req);
}