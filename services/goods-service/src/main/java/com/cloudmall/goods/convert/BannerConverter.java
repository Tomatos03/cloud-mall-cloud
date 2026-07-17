package com.cloudmall.goods.convert;

import org.mapstruct.Mapper;

import com.cloudmall.goods.api.response.BannerResp;
import com.cloudmall.goods.entity.BannerDO;

@Mapper(componentModel = "spring")
public interface BannerConverter {

    BannerResp toResp(BannerDO banner);
}
