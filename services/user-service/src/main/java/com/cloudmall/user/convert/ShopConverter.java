package com.cloudmall.user.convert;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.user.api.model.resp.ShopInfoResp;
import com.cloudmall.user.api.model.resp.ShopResp;
import com.cloudmall.user.model.entity.ShopDO;

@Mapper(componentModel = "spring")
public interface ShopConverter {

    /**
     * 转换对象数据。
     *
     * @param shop 店铺实体
     * @return 处理结果
     */
    ShopResp toResp(ShopDO shop);

    /**
     * ShopDO 列表批量转换
     *
     * @param shops 店铺实体列表
     * @return 店铺响应列表
     */
    List<ShopResp> toRespList(List<ShopDO> shops);

    /**
     * 转换店铺信息响应（统计指标由 {@link #applyStat} 填充）。
     *
     * @param shop 店铺实体
     * @return 店铺信息响应
     */
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "followCount", ignore = true)
    @Mapping(target = "positiveRate", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    ShopInfoResp toInfoResp(ShopDO shop);

    /**
     * 填充店铺统计指标。
     *
     * @param resp 店铺信息响应
     * @param rating 评分
     * @param productCount 出售中商品数量
     * @param positiveRate 好评率
     * @param followCount 关注数量
     * @return 填充后的店铺信息响应
     */
    default ShopInfoResp applyStat(
        ShopInfoResp resp,
        BigDecimal rating,
        Long productCount,
        BigDecimal positiveRate,
        Long followCount
    ) {
        resp.setRating(rating);
        resp.setProductCount(productCount);
        resp.setPositiveRate(positiveRate);
        resp.setFollowCount(followCount);
        return resp;
    }
}
