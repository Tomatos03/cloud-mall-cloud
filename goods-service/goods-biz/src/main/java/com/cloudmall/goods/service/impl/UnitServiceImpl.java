package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.goods.api.response.UnitResponse;
import com.cloudmall.goods.entity.UnitDO;
import com.cloudmall.goods.mapper.UnitMapper;
import com.cloudmall.goods.service.IUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements IUnitService {

    private final UnitMapper unitMapper;

    @Override
    public List<UnitResponse> listAll() {
        List<UnitDO> list = unitMapper.selectList(
                new LambdaQueryWrapper<UnitDO>()
                        .eq(UnitDO::getStatus, 1)
                        .orderByAsc(UnitDO::getSortOrder)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private UnitResponse toResponse(UnitDO unit) {
        UnitResponse r = new UnitResponse();
        r.setId(unit.getId());
        r.setName(unit.getName());
        return r;
    }
}
