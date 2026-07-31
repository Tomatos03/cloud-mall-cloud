package com.cloudmall.product.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.response.ProductResp;

@FeignClient(name = "product-service", contextId = "productClient", path = "/product")
public interface ProductClient {

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    Result<ProductResp> getById(@PathVariable("id") Long id);

    /**
     * 获取相关数据。
     *
     * @param spu SPU 编码
     * @return 处理结果
     */
    @GetMapping("/spu/{spu}")
    Result<ProductResp> getBySpu(@PathVariable("spu") String spu);
}
