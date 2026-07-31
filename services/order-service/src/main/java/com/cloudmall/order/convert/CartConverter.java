package com.cloudmall.order.convert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.hutool.core.util.ObjectUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.order.api.model.CartItem;
import com.cloudmall.order.api.model.CartShopGroup;
import com.cloudmall.order.api.response.CartDetailResp;
import com.cloudmall.order.entity.CartDO;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.user.api.response.ShopResp;

/**
 * 购物车对象转换器
 * <p>
 * 承担购物车记录的创建映射、条目富化、店铺分组构建与全车汇总，
 * Service 层只编排数据获取，不再手写 builder。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CartConverter {

    /**
     * SKU 实时信息 + 用户 → 购物车记录。
     * <p>
     * 新加入的购物车记录默认选中，价格/名称/图片/店铺取 SKU 实时快照。
     *
     * @param sku      SKU 实时信息
     * @param userId   用户 ID
     * @param quantity 加入数量
     * @return 购物车记录
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "shopId", source = "sku.shopId")
    @Mapping(target = "productId", source = "sku.productId")
    @Mapping(target = "skuId", source = "sku.skuId")
    @Mapping(target = "productName", source = "sku.productName")
    @Mapping(target = "productImage", source = "sku.image")
    @Mapping(target = "price", source = "sku.price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "selected", expression = "java(Boolean.TRUE)")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    CartDO toDO(SkuResp sku, Long userId, Integer quantity);

    /**
     * CartDO + SKU 实时信息 → CartItem
     * <p>
     * available、unavailableReason、subtotal 由 {@link #fillSkuState} 计算。
     *
     * @param cart 购物车记录
     * @param sku  SKU 实时信息，可为 null（商品已下架或不存在）
     * @return 购物车条目
     */
    @Mapping(target = "productId", source = "cart.productId")
    @Mapping(target = "skuId", source = "cart.skuId")
    @Mapping(target = "productName", source = "sku.productName")
    @Mapping(target = "productImage", source = "sku.image")
    @Mapping(target = "price", source = "sku.price")
    @Mapping(target = "shopId", source = "sku.shopId")
    @Mapping(target = "specDesc", source = "sku.specDesc")
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "unavailableReason", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    CartItem toItemResp(CartDO cart, SkuResp sku);

    /**
     * 批量转换购物车记录，并按 SKU 实时信息富化。
     *
     * @param carts  购物车记录列表
     * @param skuMap SKU ID -> SKU 实时信息
     * @return 购物车条目列表
     */
    default List<CartItem> toItemRespList(List<CartDO> carts, Map<Long, SkuResp> skuMap) {
        return carts.stream()
                    .map(cart -> toItemResp(cart, skuMap.get(cart.getSkuId())))
                    .toList();
    }

    /**
     * 构建店铺分组（仅做字段映射，统计项由调用方填充）。
     * <p>
     * 店铺信息缺失时名称与 Logo 留空。
     *
     * @param shopId 店铺 ID
     * @param items  该店铺下的购物车条目
     * @param shop   店铺信息，可为 null
     * @return 店铺分组
     */
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "shopLogo", source = "shop.logo")
    CartShopGroup toShopGroup(Long shopId, List<CartItem> items, ShopResp shop);

    /**
     * 构建全车摘要（仅做字段映射，汇总项由调用方填充）。
     *
     * @param groups 按店铺分组的商品列表
     * @param items  全车条目
     * @return 购物车摘要
     */
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "selectedCount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "selectedAmount", ignore = true)
    CartDetailResp toDetailResp(List<CartShopGroup> groups, List<CartItem> items);

    /**
     * 计算条目可用性与小计。
     * <p>
     * SKU 查询不到（已下架/已删除）时视为不可售，小计记 0；
     * 否则按实时价 × 数量计算小计，库存不足时标记不可售。
     *
     * @param item 购物车条目（映射目标）
     * @param cart 购物车记录
     * @param sku  SKU 实时信息，可为 null
     */
    @AfterMapping
    default void fillSkuState(@MappingTarget CartItem item, CartDO cart, SkuResp sku) {
        if (sku == null) {
            item.setAvailable(false);
            item.setUnavailableReason("商品已下架或不存在");
            item.setSubtotal(BigDecimal.ZERO);
            return;
        }

        int quantity = ObjectUtil.defaultIfNull(cart.getQuantity(), 0);
        boolean enoughStock = sku.getStock() != null && sku.getStock() >= quantity;
        item.setAvailable(enoughStock);
        if (!enoughStock) {
            item.setUnavailableReason("库存不足");
        }

        BigDecimal price = ObjectUtil.defaultIfNull(sku.getPrice(), BigDecimal.ZERO);
        item.setSubtotal(price.multiply(BigDecimal.valueOf(quantity)));
    }
}
