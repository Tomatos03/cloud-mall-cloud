package com.cloudmall.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.goods.api.request.StockReq;
import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.model.req.GoodsGetReq;
import com.cloudmall.goods.model.req.GoodsPageReq;
import com.cloudmall.goods.service.IGoodsService;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final IGoodsService goodsService;

    @GetMapping("/{id}")
    public Result<GoodsResp> getById(@PathVariable Long id) {
        GoodsGetReq req = GoodsGetReq.builder()
                                     .id(id)
                                     .build();
        return Result.success(goodsService.get(req));
    }

    @GetMapping("/spu/{spu}")
    public Result<GoodsResp> getBySpu(@PathVariable String spu) {
        GoodsGetReq req = GoodsGetReq.builder()
                                     .spu(spu)
                                     .build();
        return Result.success(goodsService.get(req));
    }
    /**
     * 通用商品列表查询。
     * <p>
     * 筛选条件通过 {@link GoodsPageReq} 的 {@code @QueryCondition} 注解声明。
     *
     * @param req 查询条件
     * @return 商品列表
     */
    @GetMapping
    public Result<IPage<GoodsResp>> page(GoodsPageReq req) {
        return Result.success(goodsService.page(req));
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
