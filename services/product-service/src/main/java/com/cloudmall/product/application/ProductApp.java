package com.cloudmall.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.service.IProductService;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.response.ShopResp;

/**
 * 商品应用服务。
 * <p>
 * 编排涉及远程调用的商品创建流程：先经 user-service 解析当前用户店铺，
 * 再委托 {@link IProductService} 完成校验与落库。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class ProductApp {

    private final ShopClient shopClient;
    private final IProductService productService;

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
