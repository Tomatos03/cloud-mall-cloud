package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.SearchReq;
import com.cloudmall.goods.api.request.StockReq;
import com.cloudmall.goods.api.response.GoodsResp;
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
    public Result<GoodsResp> getById(@PathVariable Long id) {
        return Result.success(goodsService.getById(id));
    }

    @GetMapping
    public Result<List<GoodsResp>> listByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(goodsService.listByCategory(categoryId, page, size));
    }

    @PostMapping("/search")
    public Result<List<GoodsResp>> search(@RequestBody SearchReq request) {
        return Result.success(goodsService.search(request));
    }

    @PutMapping("/{skuId}/stock")
    public Result<Boolean> updateStock(@PathVariable Long skuId, @RequestBody StockReq req) {
        Integer quantity = req.getQuantity();
        if (quantity >= 0) {
            return Result.success(goodsService.rollbackStock(skuId, quantity));
        } else {
            return Result.success(goodsService.deductStock(skuId, -quantity));
        }
    }
}
