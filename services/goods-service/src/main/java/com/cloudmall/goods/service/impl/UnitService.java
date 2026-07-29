package com.cloudmall.goods.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.api.response.UnitResp;
import com.cloudmall.goods.entity.UnitDO;
import com.cloudmall.goods.convert.UnitConverter;
import com.cloudmall.goods.mapper.UnitMapper;
import com.cloudmall.goods.service.IUnitService;

@Service
@RequiredArgsConstructor
public class UnitService implements IUnitService {

    private final UnitMapper unitMapper;
    private final UnitConverter unitConverter;

    @Override
    public List<UnitResp> list() {
        List<UnitDO> list = unitMapper.selectList(
                Wrappers.<UnitDO>lambdaQuery()
                        .eq(UnitDO::getStatus, StatusEnum.ENABLED)
                        .orderByAsc(UnitDO::getSortOrder)
        );
        return list.stream()
                   .map(unitConverter::toResp)
                   .toList();
    }
}
