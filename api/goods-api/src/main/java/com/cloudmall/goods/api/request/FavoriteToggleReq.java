package com.cloudmall.goods.api.request;

import lombok.Data;

@Data
public class FavoriteToggleReq {

    private Long userId;
    private Long goodsId;
}
