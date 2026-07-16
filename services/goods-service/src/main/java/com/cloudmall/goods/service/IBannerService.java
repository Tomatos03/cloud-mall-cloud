package com.cloudmall.goods.service;

import java.util.List;

import com.cloudmall.goods.api.response.BannerResp;

public interface IBannerService {
    List<BannerResp> listActive();
}