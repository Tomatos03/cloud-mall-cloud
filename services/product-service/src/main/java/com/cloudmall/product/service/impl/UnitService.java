package com.cloudmall.product.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.product.api.request.UnitCreateReq;
import com.cloudmall.product.api.request.UnitUpdateReq;
import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.convert.UnitConverter;
import com.cloudmall.product.entity.UnitDO;
import com.cloudmall.product.mapper.UnitMapper;
import com.cloudmall.product.model.req.UnitPageReq;
import com.cloudmall.product.service.IUnitService;

/**
 * 商品单位服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UnitService implements IUnitService {

    private final UnitMapper unitMapper;
    private final UnitConverter unitConverter;

    @Override
    public UnitResp getById(Long id) {
        UnitDO unit = unitMapper.selectById(id);
        AssertUtils.notNull(unit, BizErrorCode.DATA_NOT_FOUND);
        return unitConverter.toResp(unit);
    }

    @Override
    public IPage<UnitResp> page(UnitPageReq req) {
        return unitMapper.selectPage(req, req.buildLambdaWrapper())
                         .convert(unitConverter::toResp);
    }

    @Override
    @Transactional
    public Long create(UnitCreateReq req) {
        UnitDO unit = unitConverter.toDO(req);
        unitMapper.insert(unit);
        return unit.getId();
    }

    @Override
    @Transactional
    public void update(Long id, UnitUpdateReq req) {
        UnitDO unit = unitMapper.selectById(id);
        AssertUtils.notNull(unit, BizErrorCode.DATA_NOT_FOUND);
        unitConverter.merge(unit, req);
        unitMapper.updateById(unit);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        AssertUtils.notEmpty(ids, BizErrorCode.INVALID_PARAM);
        unitMapper.deleteBatchIds(ids);
    }
}
