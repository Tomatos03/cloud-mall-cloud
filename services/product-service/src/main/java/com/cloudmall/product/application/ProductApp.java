package com.cloudmall.product.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.CommentSummaryResp;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.convert.ProductConverter;
import com.cloudmall.product.service.IProductService;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.response.ShopInfoResp;
import com.cloudmall.user.api.response.ShopResp;

/**
 * 商品应用服务。
 * <p>
 * 编排商品创建与详情聚合：创建流程经 user-service 解析当前用户店铺；
 * 详情聚合店铺信息与评论摘要，单块获取失败降级为空，不影响其余数据。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApp {

    private final ShopClient shopClient;
    private final IProductService productService;
    private final ProductConverter productConverter;
    private final CommentApp commentApp;

    /**
     * 创建商品（SPU + 规格 + SKU + 图片）。
     *
     * @param req 创建请求
     * @return 新建 SPU ID
     */
    public Long create(ProductCreateReq req) {
        Long shopId = resolveShopId();
        return productService.create(req, shopId);
    }

    /**
     * 查询商品详情并聚合店铺信息与评论摘要。
     *
     * @param id 商品 ID
     * @return 商品完整详情
     */
    public ProductDetailResp detail(Long id) {
        ProductDetailResp detail = productService.detail(id);
        ShopInfoResp shop = loadShopInfo(detail.getShopId());
        CommentSummaryResp commentSummary = loadCommentSummary(id);
        return productConverter.applyAggregations(detail, shop, commentSummary);
    }

    /**
     * 加载店铺信息，失败时降级为空。
     *
     * @param shopId 店铺 ID
     * @return 店铺信息，获取失败时为 null
     */
    private ShopInfoResp loadShopInfo(Long shopId) {
        if (shopId == null) {
            return null;
        }
        try {
            return shopClient.getShopInfo(shopId);
        } catch (Exception e) {
            log.warn("商品详情店铺信息获取失败，降级为空，shopId={}", shopId, e);
            return null;
        }
    }

    /**
     * 加载评论摘要，失败时降级为空。
     *
     * @param productId 商品 ID
     * @return 评论摘要，获取失败时为 null
     */
    private CommentSummaryResp loadCommentSummary(Long productId) {
        try {
            return commentApp.getSummary(productId);
        } catch (Exception e) {
            log.warn("商品详情评论摘要获取失败，降级为空，productId={}", productId, e);
            return null;
        }
    }

    /**
     * 校验当前登录用户是否存在店铺，并返回店铺 ID。
     *
     * @return 店铺 ID
     */
    private Long resolveShopId() {
        Long userId = UserContextHolder.getUserId();
        ShopResp shop = Asserts.requireNonNull(
                shopClient.getByUserId(userId),
                BizError.MERCHANT_STORE_NOT_FOUND
        );
        return shop.getId();
    }
}
