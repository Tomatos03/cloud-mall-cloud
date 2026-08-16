package com.cloudmall.product.controller;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.request.ProductUpdateReq;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductDocResp;
import com.cloudmall.product.api.response.ProductResp;
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
    public Result<Long> create(@Valid @RequestBody ProductCreateReq req) {
        return Result.success(productApp.create(req));
    }

    /**
     * 更新商品（SPU 基本信息 + 规格 + SKU + 图片），并发布 ProductUpdated
     * 事件。
     *
     * @param id SPU ID
     * @param req 更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateReq req) {
        productService.update(id, req);
        return Result.success();
    }

    /**
     * 按分类 ID 集合查询已上架商品（首页分类展示用）。
     *
     * @param categoryIds 分类 ID 集合（可包含指定分类及其子分类）
     * @param limit 查询条数上限（1~50，默认 10）
     * @return 商品列表
     */
    @GetMapping
    public Result<List<ProductDocResp>> listByCategory(
        @RequestParam("categoryIds")
        Set<Long> categoryIds,
        @RequestParam(value = "limit", defaultValue = "10")
        Integer limit
    ) {
        return Result.success(productService.listByCategoryIds(categoryIds, limit));
    }

    /**
     * 根据商品 ID 获取商品详情（含品牌、图片、SKU、规格）。
     *
     * @param id 商品 ID
     * @return 商品完整详情
     */
    @GetMapping("/{id}/detail")
    public Result<ProductDetailResp> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    /**
     * 根据商品 ID 获取商品基本信息。
     *
     * @param id 商品 ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public Result<ProductResp> getById(@PathVariable Long id) {
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
    public Result<ProductResp> getBySpu(@PathVariable String spu) {
        ProductGetReq req = ProductGetReq.builder()
                                         .spuCode(spu)
                                         .build();
        return Result.success(productService.get(req));
    }

    /**
     * 游标拉取商品搜索文档（供 search-service 全量重建 / 定时增量同步使用）。
     *
     * @param req 游标分页参数（cursor / cursorTime 二选一，见
     *            {@link CursorReq}）
     * @return 游标分页结果
     */
    @GetMapping("/docs")
    public Result<CursorResult<ProductDocResp>> cursorPage(CursorReq req) {
        return Result.success(productService.cursorPage(req));
    }

    /**
     * 按商品 ID 拉取单个搜索文档（供 search-service MQ 增量消费使用）。
     *
     * @param id 商品 ID
     * @return 搜索文档，商品不存在返回 null
     */
    @GetMapping("/docs/{id}")
    public Result<ProductDocResp> searchDoc(@PathVariable Long id) {
        return Result.success(productService.searchDoc(id));
    }

    /**
     * 删除商品（软删除），并发布 ProductDeleted 事件供搜索等下游同步。
     *
     * @param id SPU ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
