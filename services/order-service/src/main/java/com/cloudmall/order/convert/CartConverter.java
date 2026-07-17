package com.cloudmall.order.convert;

import org.mapstruct.Mapper;

import com.cloudmall.order.api.response.CartResp;
import com.cloudmall.order.entity.CartDO;

@Mapper(componentModel = "spring")
public interface CartConverter {

    CartResp toResp(CartDO cart);
}
