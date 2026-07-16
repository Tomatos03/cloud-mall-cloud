package com.cloudmall.goods.service;

import java.util.List;

public interface IFavoriteService {
    boolean toggle(Long userId, Long goodsId);
    List<Long> listGoodsIds(Long userId);
    boolean isFavorited(Long userId, Long goodsId);
}
