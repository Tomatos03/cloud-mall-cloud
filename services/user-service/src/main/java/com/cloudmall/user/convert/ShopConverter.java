package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.user.api.response.ShopResp;
import com.cloudmall.user.entity.ShopDO;

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
}
