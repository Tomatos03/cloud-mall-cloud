package com.cloudmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import com.cloudmall.product.api.response.BannerResp;
import com.cloudmall.product.model.req.BannerPageReq;
import com.cloudmall.product.model.req.BannerReq;

public interface IBannerService {
    List<BannerResp> list(BannerReq req);

    IPage<BannerResp> page(BannerPageReq req);
}