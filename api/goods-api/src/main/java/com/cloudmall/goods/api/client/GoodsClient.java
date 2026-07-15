package com.cloudmall.goods.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.GoodsResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "goods-service", path = "/goods")
public interface GoodsClient {
    @GetMapping("/{id}")
    Result<GoodsResp> getById(@PathVariable("id") Long id);

    @PostMapping("/stock/deduct")
    Result<Void> deductStock(@RequestParam("skuId") Long skuId, @RequestParam("quantity") Integer quantity);

    @PostMapping("/stock/rollback")
    Result<Void> rollbackStock(@RequestParam("skuId") Long skuId, @RequestParam("quantity") Integer quantity);
}
