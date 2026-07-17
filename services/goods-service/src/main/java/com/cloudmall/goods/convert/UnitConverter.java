package com.cloudmall.goods.convert;

import org.mapstruct.Mapper;

import com.cloudmall.goods.api.response.UnitResp;
import com.cloudmall.goods.entity.UnitDO;

@Mapper(componentModel = "spring")
public interface UnitConverter {

    UnitResp toResp(UnitDO unit);
}
