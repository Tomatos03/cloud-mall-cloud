package com.cloudmall.order.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.order.api.request.CartAddReq;
import com.cloudmall.order.api.response.CartResp;
import com.cloudmall.order.entity.CartDO;

@Mapper(componentModel = "spring")
public interface CartConverter {

    CartResp toResp(CartDO cart);

    @Mapping(target = "selected", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    CartDO toDO(CartAddReq req);
}
