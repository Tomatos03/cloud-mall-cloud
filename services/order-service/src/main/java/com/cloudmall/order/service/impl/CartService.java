package com.cloudmall.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.api.model.dto.CartItem;
import com.cloudmall.order.api.model.dto.CartShopGroup;
import com.cloudmall.order.api.model.req.CartOperatorReq;
import com.cloudmall.order.api.model.req.CartSelectReq;
import com.cloudmall.order.api.model.resp.CartDetailResp;
import com.cloudmall.order.convert.CartConverter;
import com.cloudmall.order.infrastructure.cache.ICartCache;
import com.cloudmall.order.mapper.CartMapper;
import com.cloudmall.order.model.entity.CartDO;
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

    private static final Predicate<CartItem> ALL_ITEMS = item -> true;

    private final CartMapper cartMapper;
    private final CartConverter cartConverter;
    private final SkuClient skuClient;
    private final ShopClient shopClient;
    private final ICartCache cartCache;

    /**
     * 添加商品到购物车：已存在则合并数量，不存在则新增。 防重复依赖数据库唯一键 uk_user_sku，
     * 并发插入冲突时回查合并。 容量上限通过插入前校验和插入后复核控制，复核发现超限会删除本次新增并拒绝请
     * 求。 该补偿方式在正常并发下能维持容量不超限，但插入成功到复核完成之间存在窗口： 若进程在此期间崩溃
     * 、被终止或数据库连接中断，补偿删除不会执行，理论上可能残留超限记录。 购物车容量是业务软限制，该极端
     * 场景需依赖人工清理或兜底任务处理。
     *
     * @param userId 用户标识
     * @param req 请求参数
     * @return 购物车记录 ID
     */
    @Override
    public Long addToCart(Long userId, CartOperatorReq req) {
        cartCache.evict(userId);
        SkuResp sku = requireSku(req);
        CartDO existing = cartMapper.selectByUserIdAndSkuId(userId, req.getSkuId());
        if (existing != null) {
            return mergeToExisting(existing, sku.getPrice(), req.getQuantity());
        }

        checkCartCapacity(userId);
        try {
            Long cartId = insertCartItem(userId, sku, req.getQuantity());
            checkCapacityAfterInsert(userId, cartId);
            return cartId;
        } catch (DuplicateKeyException e) {
            // 唯一键 uk_user_sku 是并发下防止同一 SKU 重复入车的最终兜底
            CartDO concurrent = Asserts.requireNonNull(
                    cartMapper.selectByUserIdAndSkuId(userId, req.getSkuId()),
                    BizErrorEnum.CART_UPDATE_CONFLICT
            );
            return mergeToExisting(concurrent, sku.getPrice(), req.getQuantity());
        }
    }

    private SkuResp requireSku(CartOperatorReq req) {
        return Asserts.requireNonNull(skuClient.getById(req.getSkuId()), BizErrorEnum.CART_PRODUCT_NOT_EXIST);
    }

    /**
     * 更新相关数据。
     *
     * @param userId 用户标识
     * @param req 请求参数
     */
    @Override
    public void updateQuantity(Long userId, CartOperatorReq req) {
        cartCache.evict(userId);
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
        cartCache.evict(userId);
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
    public void selectCartItem(Long userId, CartSelectReq req) {
        cartCache.evict(userId);
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
        cartCache.evict(userId);
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
        cartCache.evict(userId);
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
        List<CartDO> carts = cartCache.getOrLoad(userId, () -> listCartItems(userId));
        if (CollUtil.isEmpty(carts)) {
            return emptyCart();
        }

        Map<Long, SkuResp> skuMap = fetchSkuMap(carts);
        Map<Long, ShopResp> shopMap = fetchShopMap(skuMap);

        List<CartItem> items = cartConverter.toItemRespList(carts, skuMap);
        List<CartShopGroup> groups = buildShopGroup(items, shopMap);

        return buildDetail(groups, items);
    }

    /**
     * 删除相关数据。
     *
     * @param userId 用户标识
     */
    @Override
    public void clear(Long userId) {
        cartCache.evict(userId);
        cartMapper.deleteByUserId(userId);
    }

    /**
     * 已存在记录：数量累加并刷新价格，返回记录 ID。
     *
     * @param existing 已有购物车记录
     * @param price SKU 实时价格
     * @param addQuantity 新增数量
     * @return 购物车记录 ID
     * @throws BizException 处理过程中可能抛出该异常
     */
    private Long mergeToExisting(CartDO existing, BigDecimal price, Integer addQuantity) {
        int expectedQuantity = existing.getQuantity();
        int newQuantity = expectedQuantity + addQuantity;
        Asserts.isTrue(newQuantity <= MAX_QUANTITY, BizErrorEnum.CART_LIMIT_EXCEEDED);
        int updated = cartMapper.mergeQuantityAndPrice(existing.getId(), newQuantity, price, expectedQuantity);
        Asserts.isTrue(updated > 0, BizErrorEnum.CART_UPDATE_CONFLICT);
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
     * 插入后复核容量：autocommit 下其他并发插入已经可见，超出上限时删除本次新增并拒绝请求。
     *
     * @param userId 用户 ID
     * @param cartId 本次新增的购物车记录 ID
     */
    private void checkCapacityAfterInsert(Long userId, Long cartId) {
        Long count = cartMapper.countByUserId(userId);
        if (count > MAX_ITEMS) {
            cartMapper.deleteById(cartId);
        }
        Asserts.isTrue(count <= MAX_ITEMS, BizErrorEnum.CART_LIMIT_EXCEEDED);
    }

    /**
     * 新增购物车记录，返回记录 ID。
     *
     * @param userId 用户 ID
     * @param sku SKU 实时信息
     * @param quantity 加入数量
     * @return 购物车记录 ID
     */
    private Long insertCartItem(Long userId, SkuResp sku, Integer quantity) {
        CartDO cart = cartConverter.toDO(sku, userId, quantity);
        cartMapper.insert(cart);
        return cart.getId();
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
        List<Long> skuIds = CollStreamUtil.toList(carts, CartDO::getSkuId);
        List<SkuResp> skus = skuClient.listSkusByIds(skuIds);
        return CollStreamUtil.toIdentityMap(skus, SkuResp::getSkuId);
    }

    /**
     * 批量查询店铺信息并转为 map（店铺 ID 取自 SKU 实时信息）。
     *
     * @param skuMap SKU ID -> SKU 实时信息
     * @return 店铺 ID -> 店铺信息
     */
    private Map<Long, ShopResp> fetchShopMap(Map<Long, SkuResp> skuMap) {
        if (CollUtil.isEmpty(skuMap)) {
            return Collections.emptyMap();
        }
        Set<Long> shopIds = CollStreamUtil.toSet(skuMap.values(), SkuResp::getShopId);
        List<ShopResp> shops = shopClient.listByIds(new ArrayList<>(shopIds));
        return CollStreamUtil.toIdentityMap(shops, ShopResp::getId);
    }

    /**
     * 按店铺分组，保持购物车加入顺序。
     *
     * @param items 购物车条目列表
     * @param shopMap 店铺 ID -> 店铺信息
     * @return 店铺分组列表
     */
    private List<CartShopGroup> buildShopGroup(List<CartItem> items, Map<Long, ShopResp> shopMap) {
        Map<Long, List<CartItem>> grouped = CollStreamUtil.groupByKey(
                items,
                item -> ObjectUtil.defaultIfNull(item.getShopId(), UNKNOWN_SHOP_ID)
        );
        return CollStreamUtil.toList(
                grouped.entrySet(),
                entry -> buildGroup(entry.getKey(), entry.getValue(), shopMap)
        );
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
        int selectedCount = CollUtil.count(items, CartItem::getSelected);
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
        resp.setSelectedCount(CollUtil.count(items, CartItem::getSelected));
        resp.setTotalAmount(sumAmount(items));
        resp.setSelectedAmount(sumAmount(items, CartItem::getSelected));
        return resp;
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
                    .filter(Objects::nonNull)
                    .filter(filter)
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 按条件汇总小计金额。
     *
     * @param items 购物车条目列表
     * @return 小计金额合计
     */
    private BigDecimal sumAmount(List<CartItem> items) {
        return sumAmount(items, ALL_ITEMS);
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
