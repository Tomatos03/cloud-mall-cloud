package com.cloudmall.goods.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.entity.GoodsDO;

@Mapper(componentModel = "spring")
public interface GoodsConverter {

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "images", ignore = true)
    GoodsResp toResp(GoodsDO goods);
}
