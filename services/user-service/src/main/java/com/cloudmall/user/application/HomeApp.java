package com.cloudmall.user.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.client.BannerClient;
import com.cloudmall.product.api.client.HomeCategoryClient;
import com.cloudmall.product.api.client.HomeProductClient;
import com.cloudmall.product.api.model.resp.BannerResp;
import com.cloudmall.product.api.model.resp.CategoryResp;
import com.cloudmall.product.api.model.resp.ProductDocResp;
import com.cloudmall.seckill.api.client.SeckillClient;
import com.cloudmall.seckill.api.model.resp.ActivityDetailResp;
import com.cloudmall.user.model.resp.HomeCategoryProductResp;
import com.cloudmall.user.model.resp.HomeResp;
import com.cloudmall.user.model.resp.NoticeResp;
import com.cloudmall.user.service.INoticeService;

/**
 * 首页聚合应用服务
 * <p>
 * 编排首页数据组装：公告、轮播图、分类、当前整点秒杀活动与分类展示商品。
 * 远程信息块通过 Feign 兜底回调降级（空列表/null），单块失败不影响其余块；
 * 本地公告查询失败时降级为空列表。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeApp {

    /** 轮播图查询条数 */
    private static final int BANNER_LIMIT = 5;

    /** 分类展示分组数量上限 */
    private static final int SECTION_LIMIT = 6;

    /** 每组标签数量上限 */
    private static final int TAB_LIMIT = 6;

    /** 每个标签商品数量上限 */
    private static final int PRODUCT_PER_TAB_LIMIT = 8;

    /** 每组商品查询条数上限 */
    private static final int PRODUCT_QUERY_LIMIT = 40;

    private final BannerClient bannerClient;
    private final HomeCategoryClient homeCategoryClient;
    private final SeckillClient seckillClient;
    private final HomeProductClient homeProductClient;
    private final INoticeService noticeService;

    /**
     * 组装首页数据。
     *
     * @return 首页聚合响应
     */
    public HomeResp getHome() {
        return HomeResp.builder()
                       .notices(loadNotices())
                       .banners(loadBanners())
                       .categories(loadCategories())
                       .seckillActivity(loadSeckillActivity())
                       .categoryProduct(loadCategoryProduct())
                       .build();
    }

    /**
     * 加载首页公告（启用中，按排序值升序）。
     *
     * @return 公告列表，失败时为空列表
     */
    private List<NoticeResp> loadNotices() {
        try {
            return noticeService.listEnabled();
        } catch (Exception e) {
            log.warn("首页公告获取失败，降级为空列表", e);
            return List.of();
        }
    }

    /**
     * 加载轮播图（复用分页端点，仅查询前 5 个，失败经兜底回调降级为空列表）。
     *
     * @return 轮播图列表
     */
    private List<BannerResp> loadBanners() {
        return bannerClient.page(1, BANNER_LIMIT).getRecords();
    }

    /**
     * 加载商品分类（平铺，含层级关系，失败经兜底回调降级为空列表）。
     *
     * @return 分类列表
     */
    private List<CategoryResp> loadCategories() {
        return homeCategoryClient.list();
    }

    /**
     * 加载当前整点时刻秒杀活动（失败经兜底回调降级为 null）。
     *
     * @return 活动详情，无进行中场次时为 null
     */
    private ActivityDetailResp loadSeckillActivity() {
        return seckillClient.getCurrentHourActivity();
    }

    /**
     * 加载分类展示商品。 <p>
     * 一级分类为展示分组，二级分类为切换标签，商品按"标签分类及其三级子分类"查询， 每个分组一次商品查询
     * 后按标签归属拆分；无商品的标签与分组丢弃。
     *
     * @return 分类展示商品分组
     */
    private List<HomeCategoryProductResp> loadCategoryProduct() {
        List<CategoryResp> categories = loadCategories();
        if (CollUtil.isEmpty(categories)) {
            return List.of();
        }

        Map<Long, CategoryResp> categoryMap = categories.stream()
                                                        .collect(
                                                                Collectors.toMap(
                                                                        CategoryResp::getId,
                                                                        Function.identity()
                                                                )
                                                        );
        Map<Long, List<Long>> childrenMap = categories
                .stream()
                .filter(cat -> !isTopCategory(cat))
                .collect(
                        Collectors.groupingBy(
                                CategoryResp::getParentId,
                                Collectors.mapping(CategoryResp::getId, Collectors.toList())
                        )
                );

        List<HomeCategoryProductResp> sections = new ArrayList<>();
        List<CategoryResp> topCategories = categories.stream()
                                                     .filter(HomeApp::isTopCategory)
                                                     .limit(SECTION_LIMIT)
                                                     .toList();
        for (CategoryResp top : topCategories) {
            HomeCategoryProductResp section = buildCategoryProductSection(top, categoryMap, childrenMap);
            if (section != null) {
                sections.add(section);
            }
        }
        return sections;
    }

    /**
     * 组装单个一级分类的商品展示分组（商品查询失败经兜底回调降级为空列表）。
     *
     * @param top 一级分类
     * @param categoryMap 分类 ID → 分类
     * @param childrenMap 父分类 ID → 子分类 ID 列表
     * @return 商品展示分组，无商品时返回 null
     */
    private HomeCategoryProductResp buildCategoryProductSection(
        CategoryResp top,
        Map<Long, CategoryResp> categoryMap,
        Map<Long, List<Long>> childrenMap
    ) {
        List<CategoryResp> tabs = CollUtil.emptyIfNull(childrenMap.get(top.getId()))
                                          .stream()
                                          .map(categoryMap::get)
                                          .filter(Objects::nonNull)
                                          .limit(TAB_LIMIT)
                                          .toList();
        if (CollUtil.isEmpty(tabs)) {
            return null;
        }

        Map<Long, List<Long>> tabQueryIds = new LinkedHashMap<>();
        Set<Long> queryIds = new LinkedHashSet<>();
        for (CategoryResp tab : tabs) {
            List<Long> ids = new ArrayList<>();
            ids.add(tab.getId());
            ids.addAll(CollUtil.emptyIfNull(childrenMap.get(tab.getId())));
            tabQueryIds.put(tab.getId(), ids);
            queryIds.addAll(ids);
        }

        int limit = Math.min(tabs.size() * PRODUCT_PER_TAB_LIMIT, PRODUCT_QUERY_LIMIT);
        List<ProductDocResp> products = homeProductClient.listByCategory(queryIds, limit);
        if (CollUtil.isEmpty(products)) {
            return null;
        }

        Map<Long, List<ProductDocResp>> groupedProduct = new LinkedHashMap<>();
        for (ProductDocResp product : products) {
            Long tabId = resolveTabId(product.getCategoryId(), tabQueryIds);
            if (tabId == null) {
                continue;
            }
            groupedProduct.computeIfAbsent(tabId, k -> new ArrayList<>()).add(product);
        }

        Map<Long, List<ProductDocResp>> productMap = new LinkedHashMap<>();
        List<CategoryResp> visibleTabs = new ArrayList<>();
        for (CategoryResp tab : tabs) {
            List<ProductDocResp> tabProduct = CollUtil.emptyIfNull(groupedProduct.get(tab.getId()))
                                                      .stream()
                                                      .limit(PRODUCT_PER_TAB_LIMIT)
                                                      .toList();
            if (CollUtil.isNotEmpty(tabProduct)) {
                visibleTabs.add(tab);
                productMap.put(tab.getId(), tabProduct);
            }
        }
        if (CollUtil.isEmpty(visibleTabs)) {
            return null;
        }

        return HomeCategoryProductResp.builder()
                                      .category(top)
                                      .tabs(visibleTabs)
                                      .productMap(productMap)
                                      .build();
    }

    /**
     * 解析商品所属的标签分类 ID。
     *
     * @param categoryId 商品分类 ID
     * @param tabQueryIds 标签分类 ID → 查询分类 ID 列表
     * @return 标签分类 ID，无法归属时返回 null
     */
    private static Long resolveTabId(Long categoryId, Map<Long, List<Long>> tabQueryIds) {
        if (categoryId == null) {
            return null;
        }
        for (Map.Entry<Long, List<Long>> entry : tabQueryIds.entrySet()) {
            if (entry.getValue().contains(categoryId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 判断是否为一级分类。
     *
     * @param category 分类
     * @return 一级分类返回 true
     */
    private static boolean isTopCategory(CategoryResp category) {
        Long parentId = category.getParentId();
        return parentId == null || parentId == 0L;
    }
}
