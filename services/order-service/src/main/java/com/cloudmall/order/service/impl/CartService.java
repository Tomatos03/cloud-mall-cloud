package com.cloudmall.order.service.impl;

import java.util.List;

import com.cloudmall.order.api.request.CartSelectReq;
import com.cloudmall.order.api.response.CartDetailResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.order.api.request.CartOperatorReq;
import com.cloudmall.order.service.ICartService;

/**
 * 购物车服务实现
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private static final int MAX_ITEMS = 100;

    @Override
    @Transactional
    public Long addToCart(Long userId, CartOperatorReq req) {
        return 1L;
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, CartOperatorReq req) {
    }

    @Override
    @Transactional
    public void removeItems(Long userId, List<Long> skuIds) {
    }

    @Override
    public void selectItems(Long userId, CartSelectReq req) {

    }

    @Override
    @Transactional
    public void selectAll(Long userId, Boolean selected) {
    }

    @Override
    @Transactional
    public void selectShop(Long userId, Long shopId, Boolean selected) {
    }

    @Override
    public CartDetailResp getCart(Long userId) {
        return null;
    }

    @Override
    @Transactional
    public void clear(Long userId) {
    }
}
