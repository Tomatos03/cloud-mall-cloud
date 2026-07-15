package com.cloudmall.user.service;

import com.cloudmall.user.api.response.StoreResp;

public interface IStoreService {
    StoreResp getByUserId(Long userId);
    StoreResp getById(Long id);
}
