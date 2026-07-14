package com.cloudmall.goods.service;

import com.cloudmall.goods.api.response.BannerResponse;

import java.util.List;

public interface IBannerService {
    List<BannerResponse> listActive();
}
