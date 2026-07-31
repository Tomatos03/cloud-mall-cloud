package com.cloudmall.product.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.response.ProductResp;

@FeignClient(name = "product-service", contextId = "productClient", path = "/product")
public interface ProductClient {

    @GetMapping("/{id}")
    Result<ProductResp> getById(@PathVariable("id") Long id);

    @GetMapping("/spu/{spu}")
    Result<ProductResp> getBySpu(@PathVariable("spu") String spu);
}
