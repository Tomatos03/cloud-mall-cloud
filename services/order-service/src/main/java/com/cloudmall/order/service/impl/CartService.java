package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.model.bo.CartItem;
import com.cloudmall.order.api.model.bo.CartShopGroup;
import com.cloudmall.order.api.model.req.CartOperatorReq;
import com.cloudmall.order.api.model.req.CartSelectReq;
import com.cloudmall.order.api.model.resp.CartDetailResp;
import com.cloudmall.order.convert.CartConverter;
import com.cloudmall.order.model.entity.CartDO;
import com.cloudmall.order.mapper.CartMapper;
import com.cloudmall.order.service.ICartService;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.model.resp.SkuResp;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.model.resp.ShopResp;

/**
 * 购物车服务实现
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    /** 购物车商品种数上限 */
    private static final int MAX_ITEMS = 100;

    /** 单个 SKU 数量上限 */
    private static final int MAX_QUANTITY = 99;

    /** 店铺信息缺失时的兜底分组 ID */
    private static final Long UNKNOWN_SHOP_ID = -1L;

    private final CartMapper cartMapper;
    private final CartConverter cartConverter;
    private final SkuClient skuClient;
    private final ShopClient shopClient;

    /**
     * 创建并保存数据。
     *
     * @param userId 用户标识
     * @param req 请求参数
     * @return 结果标识或数值
     */
    @Override
    public Long addToCart(Long userId, CartOperatorReq req) {
        SkuResp sku = fetchSku(req.getSkuId());

        CartDO existing = findCartItem(userId, req.getSkuId());
        if (existing != null) {
            return mergeToExisting(existing, sku, req.getQuantity());
        }

        checkCartCapacity(userId);
        return insertNewItem(userId, sku, req.getQuantity());
    }

    /**
     * 更新相关数据。
     *
     * @param userId 用户标识
     * @param req 请求参数
     */
    @Override
    public void updateQuantity(Long userId, CartOperatorReq req) {
        cartMapper.updateQuantityIfPositive(userId, req.getSkuId(), req.getQuantity());
    }

    /**
     * 删除相关数据。
     *
     * @param userId 用户标识
     * @param skuIds SKU 标识列表
     */
    @Override
    public void removeItems(Long userId, List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        cartMapper.deleteByUserIdAndSkuIds(userId, skuIds);
    }

    /**
     * 更新购物车商品的选中状态。
     *
     * @param userId 用户标识
     * @param req 请求参数
     */
    @Override
    public void selectItems(Long userId, CartSelectReq req) {
        int updated = cartMapper.updateSelectedByUserIdAndSkuId(userId, req.getSkuId(), req.getSelected());
        Asserts.isTrue(updated > 0, BizErrorEnum.CART_ITEM_NOT_EXIST);
    }

    /**
     * 更新购物车全选状态。
     *
     * @param userId 用户标识
     * @param selected 是否选中
     */
    @Override
    public void selectAll(Long userId, Boolean selected) {
        Asserts.notNull(selected, BizErrorEnum.INVALID_PARAM);
        cartMapper.updateSelectedByUserId(userId, selected);
    }

    /**
     * 更新店铺商品的选中状态。
     *
     * @param userId 用户标识
     * @param shopId 店铺标识
     * @param selected 是否选中
     */
    @Override
    public void selectShop(Long userId, Long shopId, Boolean selected) {
        cartMapper.updateSelectedByUserIdAndShopId(userId, shopId, selected);
    }

    /**
     * 获取相关数据。
     *
     * @param userId 用户标识
     * @return 处理结果
     */
    @Override
    public CartDetailResp getCart(Long userId) {
        List<CartDO> carts = listCartItems(userId);
        if (CollUtil.isEmpty(carts)) {
            return emptyCart();
        }

        Map<Long, SkuResp> skuMap = fetchSkuMap(carts);
        Map<Long, ShopResp> shopMap = fetchShopMap(skuMap);

        List<CartItem> items = cartConverter.toItemRespList(carts, skuMap);
        List<CartShopGroup> groups = groupByShop(items, shopMap);

        return buildDetail(groups, items);
    }

    /**
     * 删除相关数据。
     *
     * @param userId 用户标识
     */
    @Override
    public void clear(Long userId) {
        cartMapper.deleteByUserId(userId);
    }

    /**
     * 查询购物车中指定 SKU 的记录。
     *
     * @param userId 用户 ID
     * @param skuId SKU ID
     * @return 购物车记录，不存在时返回 null
     */
    private CartDO findCartItem(Long userId, Long skuId) {
        return cartMapper.selectByUserIdAndSkuId(userId, skuId);
    }

    /**
     * 已存在记录：数量累加并刷新价格，返回记录 ID。
     *
     * @param existing 已有购物车记录
     * @param sku SKU 实时信息
     * @param quantity 新增数量
     * @return 购物车记录 ID
     * @throws BizException 处理过程中可能抛出该异常
     */
    private Long mergeToExisting(CartDO existing, SkuResp sku, Integer quantity) {
        int newQuantity = existing.getQuantity() + quantity;
        Asserts.isTrue(newQuantity <= MAX_QUANTITY, BizErrorEnum.CART_LIMIT_EXCEEDED);
        cartMapper.mergeQuantityAndPrice(
                existing.getId(),
                newQuantity,
                sku.getPrice(),
                existing.getQuantity() + quantity
        );
        return existing.getId();
    }

    /**
     * 校验购物车商品种数未达上限。
     *
     * @param userId 用户 ID
     * @throws BizException 处理过程中可能抛出该异常
     */
    private void checkCartCapacity(Long userId) {
        Long count = cartMapper.countByUserId(userId);
        Asserts.isTrue(count < MAX_ITEMS, BizErrorEnum.CART_LIMIT_EXCEEDED);
    }

    /**
     * 新增购物车记录，返回记录 ID。
     *
     * @param userId 用户 ID
     * @param sku SKU 实时信息
     * @param quantity 加入数量
     * @return 购物车记录 ID
     */
    private Long insertNewItem(Long userId, SkuResp sku, Integer quantity) {
        CartDO cart = cartConverter.toDO(sku, userId, quantity);
        cartMapper.insert(cart);
        return cart.getId();
    }

    /**
     * 查询单个 SKU 的实时信息，不存在时抛出业务异常。
     *
     * @param skuId SKU ID
     * @return SKU 实时信息
     */
    private SkuResp fetchSku(Long skuId) {
        List<SkuResp> skus = fetchSkus(Collections.singletonList(skuId));
        Asserts.notEmpty(skus, BizErrorEnum.CART_PRODUCT_NOT_EXIST);
        return skus.get(0);
    }

    /**
     * 批量查询 SKU 实时信息。
     *
     * @param skuIds SKU ID 列表
     * @return SKU 信息列表，SKU 不存在或已删除时不包含对应项
     */
    private List<SkuResp> fetchSkus(List<Long> skuIds) {
        List<SkuResp> skus = skuClient.listSkusByIds(skuIds);
        return ObjectUtil.defaultIfNull(skus, Collections.emptyList());
    }

    /**
     * 批量查询店铺信息。
     *
     * @param shopIds 店铺 ID 集合
     * @return shopId -> 店铺信息
     */
    private Map<Long, ShopResp> fetchShops(Set<Long> shopIds) {
        if (CollUtil.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        List<ShopResp> shops = shopClient.listByIds(new ArrayList<>(shopIds));
        if (CollUtil.isEmpty(shops)) {
            return Collections.emptyMap();
        }
        return shops.stream().collect(Collectors.toMap(ShopResp::getId, Function.identity()));
    }

    /**
     * 查询用户购物车记录（按加入时间排序）。
     *
     * @param userId 用户 ID
     * @return 购物车记录列表
     */
    /**
     * 查询当前用户已选中的购物车项。
     *
     * @param userId 用户 ID
     * @return 已选中购物车项列表
     */
    @Override
    public List<CartDO> listSelectedItems(Long userId) {
        return cartMapper.listSelectedByUserId(userId);
    }

    private List<CartDO> listCartItems(Long userId) {
        return cartMapper.listByUserId(userId);
    }

    /**
     * 批量查询 SKU 实时信息并转为 map。
     *
     * @param carts 购物车记录列表
     * @return SKU ID -> SKU 实时信息
     */
    private Map<Long, SkuResp> fetchSkuMap(List<CartDO> carts) {
        List<Long> skuIds = carts.stream()
                                 .map(CartDO::getSkuId)
                                 .toList();
        return fetchSkus(skuIds).stream()
                                .collect(Collectors.toMap(SkuResp::getSkuId, Function.identity()));
    }

    /**
     * 批量查询店铺信息并转为 map（店铺 ID 取自 SKU 实时信息）。
     *
     * @param skuMap SKU ID -> SKU 实时信息
     * @return 店铺 ID -> 店铺信息
     */
    private Map<Long, ShopResp> fetchShopMap(Map<Long, SkuResp> skuMap) {
        Set<Long> shopIds = skuMap.values()
                                  .stream()
                                  .map(SkuResp::getShopId)
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toSet());
        return fetchShops(shopIds);
    }

    /**
     * 按店铺分组，保持购物车加入顺序。
     *
     * @param items 购物车条目列表
     * @param shopMap 店铺 ID -> 店铺信息
     * @return 店铺分组列表
     */
    private List<CartShopGroup> groupByShop(List<CartItem> items, Map<Long, ShopResp> shopMap) {
        Map<Long, List<CartItem>> grouped = items.stream()
                                                 .collect(
                                                         Collectors.groupingBy(
                                                                 item -> ObjectUtil.defaultIfNull(
                                                                         item.getShopId(),
                                                                         UNKNOWN_SHOP_ID
                                                                 ),
                                                                 LinkedHashMap::new,
                                                                 Collectors.toList()
                                                         )
                                                 );
        List<CartShopGroup> groups = new ArrayList<>(grouped.size());
        for (Map.Entry<Long, List<CartItem>> entry : grouped.entrySet()) {
            groups.add(buildGroup(entry.getKey(), entry.getValue(), shopMap));
        }
        return groups;
    }

    /**
     * 构建店铺分组：字段映射走 converter，统计项在此填充。
     *
     * @param shopId 店铺 ID
     * @param items 该店铺下的购物车条目
     * @param shopMap 店铺 ID -> 店铺信息
     * @return 店铺分组
     */
    private CartShopGroup buildGroup(Long shopId, List<CartItem> items, Map<Long, ShopResp> shopMap) {
        CartShopGroup group = cartConverter.toShopGroup(shopId, items, shopMap.get(shopId));
        int selectedCount = countSelected(items);
        group.setItemCount(items.size());
        group.setSelectedCount(selectedCount);
        group.setSelectedAmount(sumAmount(items, CartItem::getSelected));
        group.setShopSelected(CollUtil.isNotEmpty(items) && selectedCount == items.size());
        return group;
    }

    /**
     * 构建全车摘要：字段映射走 converter，汇总项在此填充。
     *
     * @param groups 按店铺分组的商品列表
     * @param items 全车条目
     * @return 购物车摘要
     */
    private CartDetailResp buildDetail(List<CartShopGroup> groups, List<CartItem> items) {
        CartDetailResp resp = cartConverter.toDetailResp(groups, items);
        resp.setTotalItems(items.size());
        int totalQuantity = items.stream()
                                 .mapToInt(item -> ObjectUtil.defaultIfNull(item.getQuantity(), 0))
                                 .sum();
        resp.setTotalQuantity(totalQuantity);
        resp.setSelectedCount(countSelected(items));
        resp.setTotalAmount(sumAmount(items, item -> true));
        resp.setSelectedAmount(sumAmount(items, CartItem::getSelected));
        return resp;
    }

    /**
     * 统计已选中条目数。
     *
     * @param items 购物车条目列表
     * @return 已选中条目数
     */
    private int countSelected(List<CartItem> items) {
        int selectedCount = 0;
        for (CartItem item : items) {
            if (BooleanUtil.isTrue(item.getSelected())) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    /**
     * 按条件汇总小计金额。
     *
     * @param items 购物车条目列表
     * @param filter 筛选条件
     * @return 小计金额合计
     */
    private BigDecimal sumAmount(List<CartItem> items, Predicate<CartItem> filter) {
        return items.stream()
                    .filter(filter)
                    .map(CartItem::getSubtotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 空购物车响应。
     *
     * @return 空购物车摘要
     */
    private CartDetailResp emptyCart() {
        return CartDetailResp.builder()
                             .groups(Collections.emptyList())
                             .totalItems(0)
                             .totalQuantity(0)
                             .selectedCount(0)
                             .totalAmount(BigDecimal.ZERO)
                             .selectedAmount(BigDecimal.ZERO)
                             .build();
    }
}
