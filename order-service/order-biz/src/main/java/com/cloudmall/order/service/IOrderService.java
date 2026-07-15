package com.cloudmall.order.service;

import com.cloudmall.order.api.request.CreateReq;
import com.cloudmall.order.api.response.OrderResp;

import java.util.List;

public interface IOrderService {

    OrderResp createOrder(CreateReq request);

    OrderResp getById(Long id);

    List<OrderResp> listByUser(Long userId, Integer page, Integer size);
}
