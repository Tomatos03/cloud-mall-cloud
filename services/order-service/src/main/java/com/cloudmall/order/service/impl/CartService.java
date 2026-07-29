package com.cloudmall.order.service.impl;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.order.api.request.CartAddReq;
import com.cloudmall.order.api.response.CartResp;
import com.cloudmall.order.convert.CartConverter;
import com.cloudmall.order.entity.CartDO;
import com.cloudmall.order.mapper.CartMapper;
import com.cloudmall.order.service.ICartService;

@Service @RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartMapper cartMapper;
    private final CartConverter cartConverter;

    @Override
    public List<CartResp> listByUser(Long userId) {
        return cartMapper.selectList(
                                 Wrappers.<CartDO>lambdaQuery().eq(CartDO::getUserId, userId)
                         )
                         .stream()
                         .map(this::toResponse)
                         .toList();
    }

    @Override
    public Long addItem(CartAddReq req) {
        CartDO existing = cartMapper.selectOne(
            Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getUserId, req.getUserId())
                .eq(CartDO::getSkuId, req.getSkuId())
        );
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            cartMapper.updateById(existing);
            return existing.getId();
        }
        CartDO cart = cartConverter.toDO(req);
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
        return cartConverter.toResp(cart);
    }
}
