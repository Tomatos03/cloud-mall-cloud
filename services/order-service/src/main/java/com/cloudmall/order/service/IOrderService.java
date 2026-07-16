package com.cloudmall.order.service;

import java.util.List;

import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.OrderResp;

public interface IOrderService {

    OrderResp createOrder(CreateReq request);

    OrderResp getById(Long id);

    List<OrderResp> listByUser(Long userId, Integer page, Integer size);
}