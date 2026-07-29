package com.cloudmall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.OrderResp;
import com.cloudmall.order.model.req.OrderPageReq;

public interface IOrderService {

    OrderResp createOrder(CreateReq request);

    OrderResp getById(Long id);

    IPage<OrderResp> page(OrderPageReq req);
}