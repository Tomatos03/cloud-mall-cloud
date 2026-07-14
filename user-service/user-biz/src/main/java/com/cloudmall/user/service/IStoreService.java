package com.cloudmall.user.service;

import com.cloudmall.user.api.response.StoreResponse;

public interface IStoreService {
    StoreResponse getByUserId(Long userId);
    StoreResponse getById(Long id);
}
