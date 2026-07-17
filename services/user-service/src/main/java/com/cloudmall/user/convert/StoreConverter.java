package com.cloudmall.user.convert;

import org.mapstruct.Mapper;

import com.cloudmall.user.api.response.StoreResp;
import com.cloudmall.user.entity.StoreDO;

@Mapper(componentModel = "spring")
public interface StoreConverter {

    StoreResp toResp(StoreDO store);
}
