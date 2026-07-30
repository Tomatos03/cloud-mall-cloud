package com.cloudmall.user.convert;

import org.mapstruct.Mapper;

import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.entity.ShopDO;

@Mapper(componentModel = "spring")
public interface ShopConverter {

    ShopResp toResp(ShopDO shop);
}
