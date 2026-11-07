package com.cloudmall.product.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.model.resp.BannerResp;
import com.cloudmall.product.model.req.BannerPageReq;
import com.cloudmall.product.model.req.BannerReq;

public interface IBannerService {

    /**
     * 查询数据列表。
     *
     * @param req 请求参数
     * @return 数据列表
     */
    List<BannerResp> list(BannerReq req);

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    IPage<BannerResp> page(BannerPageReq req);
}
