package com.cloudmall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.convert.UnitConverter;
import com.cloudmall.product.entity.UnitDO;
import com.cloudmall.product.mapper.UnitMapper;
import com.cloudmall.product.model.req.UnitPageReq;
import com.cloudmall.product.service.IUnitService;

@Service
@RequiredArgsConstructor
public class UnitService implements IUnitService {

    private final UnitMapper unitMapper;
    private final UnitConverter unitConverter;

    @Override
    public IPage<UnitResp> page(UnitPageReq req) {
        return unitMapper.selectPage(req, req.buildLambdaWrapper())
                         .convert(unitConverter::toResp);
    }
}
