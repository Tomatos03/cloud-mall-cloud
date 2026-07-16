package com.cloudmall.order.service.impl;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.order.api.response.CartResp;
import com.cloudmall.order.entity.CartDO;
import com.cloudmall.order.mapper.CartMapper;
import com.cloudmall.order.service.ICartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class CartServiceImpl implements ICartService {
    private final CartMapper cartMapper;

    @Override
    public List<CartResp> listByUser(Long userId) {
        return cartMapper.selectList(
            new LambdaQueryWrapper<CartDO>().eq(CartDO::getUserId, userId)
        ).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Long addItem(Long userId, Long goodsId, Long skuId, String goodsName, String goodsImage, BigDecimal price, Integer quantity) {
        // Check if already in cart
        CartDO existing = cartMapper.selectOne(
            new LambdaQueryWrapper<CartDO>()
                .eq(CartDO::getUserId, userId)
                .eq(CartDO::getSkuId, skuId)
        );
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
            return existing.getId();
        }
        CartDO cart = new CartDO();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setSkuId(skuId);
        cart.setGoodsName(goodsName);
        cart.setGoodsImage(goodsImage);
        cart.setPrice(price);
        cart.setQuantity(quantity);
        cart.setSelected(true);
        cart.setCreateTime(LocalDateTime.now());
        cartMapper.insert(cart);
        return cart.getId();
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        CartDO cart = cartMapper.selectById(id);
        if (cart == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
    }

    @Override
    public void deleteItem(Long id) { cartMapper.deleteById(id); }

    @Override
    public void clearByUser(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<CartDO>().eq(CartDO::getUserId, userId));
    }

    private CartResp toResponse(CartDO cart) {
        CartResp r = new CartResp();
        r.setId(cart.getId());
        r.setGoodsId(cart.getGoodsId());
        r.setSkuId(cart.getSkuId());
        r.setGoodsName(cart.getGoodsName());
        r.setGoodsImage(cart.getGoodsImage());
        r.setPrice(cart.getPrice());
        r.setQuantity(cart.getQuantity());
        r.setSelected(cart.getSelected());
        return r;
    }
}
