package com.cloudmall.coupon.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.api.dto.CouponCalcItem;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.enums.CouponScopeEnum;
import com.cloudmall.coupon.mapper.CouponCategoryMapper;
import com.cloudmall.coupon.mapper.CouponProductMapper;
import com.cloudmall.product.api.client.CategoryClient;
import com.cloudmall.product.api.response.CategoryResp;

/**
 * 优惠券范围匹配器
 * <p>
 * 负责 ALL / PRODUCT / CATEGORY 三种适用范围判定。
 * 店铺券先按归属店铺过滤，再按范围类型过滤；分类范围支持命中后代分类。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class CouponScopeMatcher {

    private final CouponProductMapper couponProductMapper;
    private final CouponCategoryMapper couponCategoryMapper;
    private final CategoryClient categoryClient;

    /**
     * 匹配模板范围内的商品明细（店铺券先按归属店铺过滤）。
     *
     * @param template 模板
     * @param items 商品明细列表
     * @return 范围内明细
     */
    public List<CouponCalcItem> matchItems(CouponDO template, List<CouponCalcItem> items) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<CouponCalcItem> shopItems = filterByShop(template, items);
        if (CollUtil.isEmpty(shopItems)) {
            return Collections.emptyList();
        }
        CouponScopeEnum scopeType = template.getScopeType();
        if (scopeType == CouponScopeEnum.ALL) {
            return shopItems;
        }
        if (scopeType == CouponScopeEnum.PRODUCT) {
            return filterByProducts(template.getId(), shopItems);
        }
        return filterByCategories(template.getId(), shopItems);
    }

    /**
     * 店铺券按归属店铺过滤，平台券不过滤。
     *
     * @param template 模板
     * @param items 商品明细列表
     * @return 过滤后明细
     */
    private List<CouponCalcItem> filterByShop(CouponDO template, List<CouponCalcItem> items) {
        if (template.isPlatform()) {
            return items;
        }
        return items.stream()
                    .filter(item -> Objects.equals(item.getShopId(), template.getShopId()))
                    .toList();
    }

    /**
     * 按指定商品范围过滤。
     *
     * @param couponId 模板 ID
     * @param items 商品明细列表
     * @return 命中商品范围的明细
     */
    private List<CouponCalcItem> filterByProducts(Long couponId, List<CouponCalcItem> items) {
        Set<Long> productIds = loadProductIds(couponId);
        return items.stream()
                    .filter(item -> productIds.contains(item.getProductId()))
                    .toList();
    }

    /**
     * 按指定分类范围过滤（含后代分类）。
     *
     * @param couponId 模板 ID
     * @param items 商品明细列表
     * @return 命中分类范围的明细
     */
    private List<CouponCalcItem> filterByCategories(Long couponId, List<CouponCalcItem> items) {
        Set<Long> targetIds = loadCategoryIds(couponId);
        Set<Long> hitIds = resolveHitCategoryIds(targetIds, items);
        return items.stream()
                    .filter(item -> hitIds.contains(item.getCategoryId()))
                    .toList();
    }

    /**
     * 加载模板指定商品 ID 集合。
     *
     * @param couponId 模板 ID
     * @return 商品 ID 集合
     */
    private Set<Long> loadProductIds(Long couponId) {
        return new HashSet<>(couponProductMapper.selectIdsByCouponId(couponId));
    }

    /**
     * 加载模板指定分类 ID 集合。
     *
     * @param couponId 模板 ID
     * @return 分类 ID 集合
     */
    private Set<Long> loadCategoryIds(Long couponId) {
        return new HashSet<>(couponCategoryMapper.selectIdsByCouponId(couponId));
    }

    /**
     * 解析目标分类及其全部后代分类 ID（含自身）。
     *
     * @param targetIds 模板目标分类 ID 集合
     * @param items 商品明细列表
     * @return 命中分类 ID 集合
     */
    private Set<Long> resolveHitCategoryIds(Set<Long> targetIds, List<CouponCalcItem> items) {
        if (CollUtil.isEmpty(targetIds) || CollUtil.isEmpty(items)) {
            return Collections.emptySet();
        }
        Set<Long> involved = new HashSet<>(targetIds);
        items.forEach(item -> involved.add(item.getCategoryId()));
        Map<Long, Long> parentMap = loadParentMap(involved);
        Set<Long> hitIds = new HashSet<>();
        for (Long targetId : targetIds) {
            collectWithDescendants(targetId, parentMap, hitIds);
        }
        return hitIds;
    }

    /**
     * 批量查询分类父子映射（id → parentId）。
     *
     * @param categoryIds 涉及分类 ID 集合
     * @return 父子映射
     */
    private Map<Long, Long> loadParentMap(Set<Long> categoryIds) {
        Map<Long, Long> parentMap = new HashMap<>();
        Set<Long> pending = new HashSet<>(categoryIds);
        Set<Long> queried = new HashSet<>();
        while (CollUtil.isNotEmpty(pending)) {
            List<Long> batch = new ArrayList<>(pending);
            pending.clear();
            List<CategoryResp> categoryList = categoryClient.getByIds(batch);
            for (CategoryResp cat : categoryList) {
                queried.add(cat.getId());
                Long parentId = cat.getParentId();
                if (parentId == null || parentId == 0) {
                    continue;
                }
                parentMap.put(cat.getId(), parentId);
                if (!queried.contains(parentId)) {
                    pending.add(parentId);
                }
            }
        }
        return parentMap;
    }

    /**
     * 收集指定分类及其全部后代分类 ID。
     *
     * @param id 分类 ID
     * @param parentMap 父子映射
     * @param hitIds 结果集合
     */
    private void collectWithDescendants(Long id, Map<Long, Long> parentMap, Set<Long> hitIds) {
        if (id == null || !hitIds.add(id)) {
            return;
        }
        for (Map.Entry<Long, Long> entry : parentMap.entrySet()) {
            if (Objects.equals(entry.getValue(), id)) {
                collectWithDescendants(entry.getKey(), parentMap, hitIds);
            }
        }
    }
}
