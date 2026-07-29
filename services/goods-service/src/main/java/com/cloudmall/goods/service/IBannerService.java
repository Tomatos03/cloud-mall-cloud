package com.cloudmall.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.model.req.BannerPageReq;
import com.cloudmall.goods.model.req.BannerReq;

public interface IBannerService {
    List<BannerResp> list(BannerReq req);

    IPage<BannerResp> page(BannerPageReq req);
}