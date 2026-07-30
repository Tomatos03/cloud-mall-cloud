package com.cloudmall.product.service;

import java.util.List;

public interface IFavoriteService {
    boolean toggle(Long userId, Long productId);
    List<Long> listProductIds(Long userId);
    boolean isFavorited(Long userId, Long productId);
}
