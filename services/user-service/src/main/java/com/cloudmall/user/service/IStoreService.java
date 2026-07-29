package com.cloudmall.user.service;

import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.model.req.StoreGetReq;

public interface IStoreService {
    StoreResp get(StoreGetReq req);
}
