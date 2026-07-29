package com.cloudmall.order.service;

import java.util.List;

import com.cloudmall.order.api.request.CartAddReq;
import com.cloudmall.order.api.response.CartResp;

public interface ICartService {
    List<CartResp> listByUser(Long userId);
    Long addItem(CartAddReq req);
    void updateQuantity(Long id, Integer quantity);
    void deleteItem(Long id);
    void clearByUser(Long userId);
}