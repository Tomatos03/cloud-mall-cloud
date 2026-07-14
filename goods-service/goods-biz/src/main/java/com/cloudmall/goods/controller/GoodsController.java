package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.GoodsSearchRequest;
import com.cloudmall.goods.api.response.GoodsResponse;
import com.cloudmall.goods.service.IGoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final IGoodsService goodsService;

    @GetMapping("/{id}")
    public Result<GoodsResponse> getById(@PathVariable Long id) {
        return Result.success(goodsService.getById(id));
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<GoodsResponse>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(goodsService.listByCategory(categoryId, page, size));
    }

    @PostMapping("/search")
    public Result<List<GoodsResponse>> search(@RequestBody GoodsSearchRequest request) {
        return Result.success(goodsService.search(request));
    }

    @PostMapping("/stock/deduct")
    public Result<Boolean> deductStock(@RequestParam Long skuId, @RequestParam Integer quantity) {
        return Result.success(goodsService.deductStock(skuId, quantity));
    }

    @PostMapping("/stock/rollback")
    public Result<Boolean> rollbackStock(@RequestParam Long skuId, @RequestParam Integer quantity) {
        return Result.success(goodsService.rollbackStock(skuId, quantity));
    }
}
