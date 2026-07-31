package com.cloudmall.product.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.SkuMatchReq;
import com.cloudmall.product.api.request.StockReq;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.product.service.ISkuService;

/**
 * 商品 SKU Controller。
 * <p>
 * 提供 SKU 库存管理、批量查询等端点。
 *
 * @author : Tomatos
 */
@RestController
@RequestMapping("/skus")
@RequiredArgsConstructor
public class SkuController {

    private final ISkuService skuService;

    /**
     * 更新商品 SKU 库存。
     * <p>
     * quantity >= 0 时回滚库存，quantity < 0 时扣减库存。
     *
     * @param skuId SKU ID
     * @param req   库存变更请求
     * @return 操作是否成功
     */
    @PutMapping("/{skuId}/stock")
    public Result<Boolean> updateStock(@PathVariable Long skuId, @RequestBody StockReq req) {
        return Result.success(skuService.updateStock(skuId, req.getQuantity()));
    }

    /**
     * 批量查询 SKU 信息（含实时价格、库存、上下架状态、规格描述、所属店铺）。
     * <p>
     * 供购物车、订单等场景一次取齐，避免逐条调用。
     *
     * @param skuIds SKU ID 列表
     * @return SKU 信息列表
     */
    @PostMapping("/batch-query")
    public Result<List<SkuResp>> listSkusByIds(@RequestBody List<Long> skuIds) {
        return Result.success(skuService.listSkusByIds(skuIds));
    }

    /**
     * 按已选规格值匹配唯一 SKU（详情页规格选择联动）。
     * <p>
     * 所有规格组均有选值时唯一命中一个 SKU；未选全或组合不成立时返回 null。
     *
     * @param req 商品 ID + 已选规格值 ID 列表
     * @return 唯一命中的 SKU 信息，未命中时为 null
     */
    @PostMapping("/match")
    public Result<SkuResp> matchSku(@RequestBody SkuMatchReq req) {
        return Result.success(skuService.matchSkuBySpec(req.getProductId(), req.getSpecValueIds()));
    }
}
