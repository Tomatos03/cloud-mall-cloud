package com.cloudmall.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.goods.api.response.UnitResp;
import com.cloudmall.goods.model.req.UnitPageReq;

public interface IUnitService {
    IPage<UnitResp> page(UnitPageReq req);
}