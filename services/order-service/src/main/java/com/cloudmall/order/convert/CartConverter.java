package com.cloudmall.order.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.order.api.model.CartItem;
import com.cloudmall.order.entity.CartDO;

/**
 * 购物车对象转换器
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CartConverter {

    /**
     * CartDO → CartItem（基础字段映射）
     * <p>
     * shopId、specDesc、available、unavailableReason、subtotal
     * 由调用方在 Service 层根据商品/店铺服务数据补充。
     */
    @Mapping(target = "productId", source = "goodsId")
    @Mapping(target = "productName", source = "goodsName")
    @Mapping(target = "productImage", source = "goodsImage")
    @Mapping(target = "shopId", ignore = true)
    @Mapping(target = "specDesc", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "unavailableReason", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    CartItem toItemResp(CartDO cart);
}
