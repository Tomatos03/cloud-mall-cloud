package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.api.response.UnitResp;
import com.cloudmall.goods.convert.UnitConverter;
import com.cloudmall.goods.entity.UnitDO;
import com.cloudmall.goods.mapper.UnitMapper;
import com.cloudmall.goods.model.req.UnitPageReq;
import com.cloudmall.goods.service.IUnitService;

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
