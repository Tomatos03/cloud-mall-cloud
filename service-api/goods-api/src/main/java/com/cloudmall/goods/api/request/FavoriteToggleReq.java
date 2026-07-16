package com.cloudmall.goods.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FavoriteToggleReq {

    private Long userId;
    private Long goodsId;
}