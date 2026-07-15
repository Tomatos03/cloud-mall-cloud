package com.cloudmall.goods.service;

import com.cloudmall.goods.api.response.BannerResp;

import java.util.List;

public interface IBannerService {
    List<BannerResp> listActive();
}
