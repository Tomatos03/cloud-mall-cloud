package com.cloudmall.product.controller;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.api.response.SearchDocPageResp;
import com.cloudmall.product.application.ProductApp;
import com.cloudmall.product.model.req.ProductGetReq;
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
    private final ProductApp productApp;

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
        return Result.success(productApp.create(req));
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
     * 分页拉取商品搜索文档（search-service 数据源）。 <p>
     * 按 updateTime 升序排列，支持 since 增量游标；包含下架商品（status 透传），
     * 由消费方决定写入或丢弃。文档不含 sales，避免覆盖 ES 中的销量。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @param since 增量游标（可选），ISO 日期时间
     * @return 搜索文档分页结果
     */
    @GetMapping("/search-docs")
    public Result<SearchDocPageResp> searchDocs(
            @RequestParam(value = "page", defaultValue = "1")
            long page,
            @RequestParam(value = "size", defaultValue = "100")
            long size,
            @RequestParam(value = "since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since
    ) {
        return Result.success(productService.searchDocs(page, size, since));
    }
}
