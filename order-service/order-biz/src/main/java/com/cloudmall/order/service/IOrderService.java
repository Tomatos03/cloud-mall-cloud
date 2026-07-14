package com.cloudmall.order.service;

import com.cloudmall.order.api.request.OrderCreateRequest;
import com.cloudmall.order.api.response.OrderResponse;

import java.util.List;

public interface IOrderService {

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getById(Long id);

    List<OrderResponse> listByUser(Long userId, Integer page, Integer size);
}
