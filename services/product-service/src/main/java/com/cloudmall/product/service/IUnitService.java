package com.cloudmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.model.req.UnitPageReq;

public interface IUnitService {
    IPage<UnitResp> page(UnitPageReq req);
}