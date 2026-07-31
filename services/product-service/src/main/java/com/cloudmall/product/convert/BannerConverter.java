package com.cloudmall.product.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.product.api.response.BannerResp;
import com.cloudmall.product.entity.BannerDO;

@Mapper(componentModel = "spring")
public interface BannerConverter {

    /**
     * 转换对象数据。
     *
     * @param banner 轮播图实体
     * @return 处理结果
     */
    BannerResp toResp(BannerDO banner);

    /**
     * 转换对象数据。
     *
     * @param banners 轮播图实体列表
     * @return 数据列表
     */
    List<BannerResp> toRespList(List<BannerDO> banners);
}
