package com.cloudmall.user.service;

import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.model.req.ShopGetReq;

public interface IShopService {
    ShopResp get(ShopGetReq req);
}
