package com.cloudmall.order.service.impl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.order.api.response.CartResp;
import com.cloudmall.order.entity.CartDO;
import com.cloudmall.order.mapper.CartMapper;
import com.cloudmall.order.service.ICartService;

@Service @RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartMapper cartMapper;

    @Override
    public List<CartResp> listByUser(Long userId) {
        return cartMapper.selectList(
            Wrappers.<CartDO>lambdaQuery().eq(CartDO::getUserId, userId)
        ).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Long addItem(Long userId, Long goodsId, Long skuId, String goodsName, String goodsImage, BigDecimal price, Integer quantity) {
        // Check if already in cart
        CartDO existing = cartMapper.selectOne(
            Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getUserId, userId)
                .eq(CartDO::getSkuId, skuId)
        );
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
            return existing.getId();
        }
        CartDO cart = CartDO.builder()
                .userId(userId)
                .goodsId(goodsId)
                .skuId(skuId)
                .goodsName(goodsName)
                .goodsImage(goodsImage)
                .price(price)
                .quantity(quantity)
                .selected(true)
                .createTime(LocalDateTime.now())
                .build();
        cartMapper.insert(cart);
        return cart.getId();
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        CartDO cart = cartMapper.selectById(id);
        AssertUtils.notNull(cart, BizErrorCode.DATA_NOT_FOUND);
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
    }

    @Override
    public void deleteItem(Long id) { cartMapper.deleteById(id); }

    @Override
    public void clearByUser(Long userId) {
        cartMapper.delete(Wrappers.<CartDO>lambdaQuery().eq(CartDO::getUserId, userId));
    }

    private CartResp toResponse(CartDO cart) {
        return CartResp.builder()
                .id(cart.getId())
                .goodsId(cart.getGoodsId())
                .skuId(cart.getSkuId())
                .goodsName(cart.getGoodsName())
                .goodsImage(cart.getGoodsImage())
                .price(cart.getPrice())
                .quantity(cart.getQuantity())
                .selected(cart.getSelected())
                .build();
    }
}
