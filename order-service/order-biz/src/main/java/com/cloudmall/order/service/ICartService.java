package com.cloudmall.order.service;
import com.cloudmall.order.api.response.CartResponse;
import java.util.List;

public interface ICartService {
    List<CartResponse> listByUser(Long userId);
    Long addItem(Long userId, Long goodsId, Long skuId, String goodsName, String goodsImage, java.math.BigDecimal price, Integer quantity);
    void updateQuantity(Long id, Integer quantity);
    void deleteItem(Long id);
    void clearByUser(Long userId);
}
