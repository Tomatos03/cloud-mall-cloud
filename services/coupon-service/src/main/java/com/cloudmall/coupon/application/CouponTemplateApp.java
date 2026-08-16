package com.cloudmall.coupon.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.request.CouponReq;
import com.cloudmall.coupon.service.ICouponTemplateService;
import com.cloudmall.product.api.client.CategoryClient;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.api.response.ProductResp;

/**
 * 优惠券模板应用服务。
 * <p>
 * 编排涉及远程调用的模板创建与编辑流程：先解析店铺归属并校验适用范围中商品/分类
 * 存在性（product-service），再委托 {@link ICouponTemplateService} 完成本地校验与
 * 落库。 不含远程调用的状态流转与查询仍直接走服务层。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateApp {

    private final ICouponTemplateService couponTemplateService;
    private final ProductClient productClient;
    private final CategoryClient categoryClient;

    /**
     * 创建模板草稿。 <p>
     * ownerType=SHOP 时 shopId 取自登录令牌；平台券 shopId 恒为空。
     *
     * @param req 创建请求
     * @return 新模板 ID
     */
    public Long createDraft(CouponReq req) {
        Long shopId = resolveCreateShopId(req);
        validateScope(req, shopId);
        return couponTemplateService.createDraft(req, shopId);
    }

    /**
     * 编辑模板草稿，仅 DRAFT 状态可编辑。 <p>
     * 店铺归属以已存在模板为准，再校验请求中的适用范围。
     *
     * @param id 模板 ID
     * @param req 编辑请求
     */
    public void updateDraft(Long id, CouponReq req) {
        Long shopId = couponTemplateService.getTemplate(id).getShopId();
        validateScope(req, shopId);
        couponTemplateService.updateDraft(id, req, shopId);
    }

    /**
     * 解析创建模板的店铺 ID：平台券为 null，商家强制当前店铺，运营按请求指定。
     *
     * @param req 创建请求
     * @return 店铺 ID（平台券为 null）
     */
    private Long resolveCreateShopId(CouponReq req) {
        if (req.isPlatform()) {
            return null;
        }
        if (UserContextHolder.isAdmin()) {
            return req.getShopId();
        }
        return UserContextHolder.getShopId();
    }

    /**
     * 校验适用范围配置（商品/分类存在性，店铺券目标商品必须属于当前店铺）。
     *
     * @param req 请求参数
     * @param shopId 店铺 ID
     */
    private void validateScope(CouponReq req, Long shopId) {
        if (req.isProductScope()) {
            List<Long> distinct = req.getProductIds()
                                     .stream()
                                     .distinct()
                                     .toList();
            for (Long productId : distinct) {
                ProductResp product = requireProduct(productId);
                if (req.isShop()) {
                    Asserts.isTrue(shopId.equals(product.getShopId()), BizError.COUPON_SCOPE_INVALID);
                }
            }
        } else if (req.isCategoryScope()) {
            List<Long> distinct = req.getCategoryIds()
                                     .stream()
                                     .distinct()
                                     .toList();
            for (Long categoryId : distinct) {
                requireCategory(categoryId);
            }
        }
    }

    /**
     * 调用商品服务校验商品存在性。
     *
     * @param productId 商品 ID
     * @return 商品信息
     */
    private ProductResp requireProduct(Long productId) {
        return Asserts.requireNonNull(productClient.getById(productId), BizError.PRODUCT_NOT_EXIST);
    }

    /**
     * 调用商品服务校验分类存在性。
     *
     * @param categoryId 分类 ID
     * @return 分类信息
     */
    private CategoryResp requireCategory(Long categoryId) {
        return Asserts.requireNonNull(
                categoryClient.getById(categoryId),
                BizError.CATEGORY_NOT_EXIST_OR_NO_ENABLE
        );
    }
}
