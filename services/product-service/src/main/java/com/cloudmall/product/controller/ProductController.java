package com.cloudmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.model.req.ProductGetReq;
import com.cloudmall.product.model.req.ProductPageReq;
import com.cloudmall.product.service.IProductService;

/**
 * 商品 Controller。
 * <p>
 * 提供 SPU 查询、创建等端点。SKU 相关端点见 {@link com.cloudmall.product.controller.SkuController}。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService;

    /**
     * 创建商品（SPU + 规格 + SKU + 图片）。
     *
     * @param req 创建请求
     * @return 新建 SPU ID
     */
    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody
            ProductCreateReq req
    ) {
        return Result.success(productService.create(req));
    }

    /**
     * 根据商品 ID 获取商品详情（含品牌、图片、SKU、规格）。
     *
     * @param id 商品 ID
     * @return 商品完整详情
     */
    @GetMapping("/{id}/detail")
    public Result<ProductDetailResp> detail(
            @PathVariable
            Long id
    ) {
        return Result.success(productService.detail(id));
    }

    /**
     * 根据商品 ID 获取商品基本信息。
     *
     * @param id 商品 ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public Result<ProductResp> getById(
            @PathVariable
            Long id
    ) {
        ProductGetReq req = ProductGetReq.builder()
                                         .id(id)
                                         .build();
        return Result.success(productService.get(req));
    }

    /**
     * 根据 SPU 编码获取商品基本信息。
     *
     * @param spu SPU 编码
     * @return 商品信息
     */
    @GetMapping("/spu/{spu}")
    public Result<ProductResp> getBySpu(
            @PathVariable
            String spu
    ) {
        ProductGetReq req = ProductGetReq.builder()
                                         .spuCode(spu)
                                         .build();
        return Result.success(productService.get(req));
    }

    /**
     * 通用商品列表查询。 <p>
     * 筛选条件通过 {@link ProductPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 查询条件
     * @return 商品列表
     */
    @GetMapping
    public Result<IPage<ProductResp>> page(ProductPageReq req) {
        return Result.success(productService.page(req));
    }
}
