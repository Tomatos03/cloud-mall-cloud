package com.cloudmall.product.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.StockReq;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.api.response.SkuResp;

@FeignClient(name = "product-service", path = "/product")
public interface ProductClient {

    @GetMapping("/{id}")
    Result<ProductResp> getById(@PathVariable("id") Long id);

    @GetMapping("/spu/{spu}")
    Result<ProductResp> getBySpu(@PathVariable("spu") String spu);

    /**
     * 批量查询 SKU 信息（含实时价格、库存、上下架状态、规格描述、所属店铺）。
     * <p>
     * 供购物车、订单等场景一次取齐，避免逐条调用。
     *
     * @param skuIds SKU ID 列表
     * @return SKU 信息列表
     */
    @PostMapping("/skus/batch-query")
    Result<List<SkuResp>> listSkusByIds(@RequestBody List<Long> skuIds);

    @PutMapping("/{skuId}/stock")
    Result<Boolean> updateStock(@PathVariable("skuId") Long skuId, @RequestBody StockReq req);
}