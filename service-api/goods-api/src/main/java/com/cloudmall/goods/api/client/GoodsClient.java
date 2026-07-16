package com.cloudmall.goods.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.StockReq;
import com.cloudmall.goods.api.response.GoodsResp;

@FeignClient(name = "goods-service", path = "/goods")
public interface GoodsClient {

    @GetMapping("/{id}")
    Result<GoodsResp> getById(@PathVariable("id") Long id);

    @PutMapping("/{skuId}/stock")
    Result<Boolean> updateStock(@PathVariable("skuId") Long skuId, @RequestBody StockReq req);
}