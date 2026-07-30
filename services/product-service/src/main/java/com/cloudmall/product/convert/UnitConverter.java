package com.cloudmall.product.convert;

import org.mapstruct.Mapper;

import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.entity.UnitDO;

@Mapper(componentModel = "spring")
public interface UnitConverter {

    UnitResp toResp(UnitDO unit);
}
