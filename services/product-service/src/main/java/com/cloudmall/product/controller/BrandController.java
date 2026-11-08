package com.cloudmall.product.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.model.req.BrandCreateReq;
import com.cloudmall.product.api.model.req.BrandUpdateReq;
import com.cloudmall.product.api.model.resp.BrandResp;
import com.cloudmall.product.api.model.resp.ProductResp;
import com.cloudmall.product.model.req.BrandPageReq;
import com.cloudmall.product.model.req.BrandProductPageReq;
import com.cloudmall.product.service.IBrandService;
import com.cloudmall.product.service.IProductService;

/**
 * 品牌 Controller。
 * <p>
 * 提供品牌的增删改查功能。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/brands")
public class BrandController {

    private final IBrandService brandService;
    private final IProductService productService;

    /**
     * 游标查询启用品牌（按品牌 ID 升序，供品牌专场滚动加载）。
     *
     * @param req 游标请求（首次 cursor=0，size 默认 10）
     * @return 游标分页结果
     */
    @GetMapping("/cursor")
    public Result<CursorResult<BrandResp>> cursor(CursorReq req) {
        return Result.success(brandService.cursor(req));
    }

    /**
     * 根据 ID 获取品牌。
     *
     * @param id 品牌 ID
     * @return 品牌信息
     */
    @GetMapping("/{id}")
    public Result<BrandResp> getById(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }

    /**
     * 分页查询品牌下的已上架商品。
     *
     * @param id 品牌 ID
     * @param req 分页请求
     * @return 商品分页数据
     */
    @GetMapping("/{id}/products")
    public Result<IPage<ProductResp>> products(@PathVariable Long id, BrandProductPageReq req) {
        brandService.getById(id);
        return Result.success(productService.pageByBrand(id, req));
    }

    /**
     * 游标查询品牌下的已上架商品（按商品 ID 倒序，供品牌详情滚动加载）。
     *
     * @param id 品牌 ID
     * @param req 游标请求（首次 cursor=0，size 默认 10）
     * @return 游标分页结果
     */
    @GetMapping("/{id}/products/cursor")
    public Result<CursorResult<ProductResp>> productCursor(@PathVariable Long id, CursorReq req) {
        brandService.getById(id);
        return Result.success(productService.cursorByBrand(id, req));
    }

    /**
     * 分页查询品牌列表。
     *
     * @param req 分页 + 筛选条件
     * @return 品牌分页列表
     */
    @GetMapping
    public Result<IPage<BrandResp>> page(BrandPageReq req) {
        return Result.success(brandService.page(req));
    }

    /**
     * 创建品牌。
     *
     * @param req 创建请求
     * @return 新建品牌 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody BrandCreateReq req) {
        return Result.success(brandService.create(req));
    }

    /**
     * 更新品牌。
     *
     * @param id 品牌 ID
     * @param req 更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BrandUpdateReq req) {
        brandService.update(id, req);
        return Result.success();
    }

    /**
     * 批量删除品牌（软删除）。
     *
     * @param ids 品牌 ID 列表
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> delete(@RequestBody List<Long> ids) {
        brandService.delete(ids);
        return Result.success();
    }
}
