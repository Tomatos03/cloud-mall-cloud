package com.cloudmall.product.service;

import java.util.List;

import com.cloudmall.common.model.PageResp;
import com.cloudmall.product.api.model.req.BannerPageReq;
import com.cloudmall.product.api.model.req.BannerReq;
import com.cloudmall.product.api.model.resp.BannerResp;

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
    PageResp<BannerResp> page(BannerPageReq req);
}
