package com.cloudmall.goods.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.StockReq;
import com.cloudmall.goods.api.response.GoodsResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "goods-service", path = "/goods")
public interface GoodsClient {

    @GetMapping("/{id}")
    Result<GoodsResp> getById(@PathVariable("id") Long id);

    @PutMapping("/{skuId}/stock")
    Result<Boolean> updateStock(@PathVariable("skuId") Long skuId, @RequestBody StockReq req);
}
