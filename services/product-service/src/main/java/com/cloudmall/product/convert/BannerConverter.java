package com.cloudmall.product.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.product.api.response.BannerResp;
import com.cloudmall.product.entity.BannerDO;

@Mapper(componentModel = "spring")
public interface BannerConverter {

    BannerResp toResp(BannerDO banner);

    List<BannerResp> toRespList(List<BannerDO> banners);
}
