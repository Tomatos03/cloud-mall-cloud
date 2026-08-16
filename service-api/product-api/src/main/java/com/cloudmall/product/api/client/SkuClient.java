package com.cloudmall.product.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.product.api.request.StockReq;
import com.cloudmall.product.api.response.SkuResp;

/**
 * 商品 SKU Feign 客户端。
 */
@FeignClient(name = "product-service", contextId = "skuClient", path = "/skus")
public interface SkuClient {

    /**
     * 更新商品 SKU 库存。 <p>
     * quantity 为正数时增加库存，为负数时扣减库存。
     *
     * @param skuId SKU ID
     * @param req 库存变更请求
     * @return 操作是否成功
     */
    @PutMapping("/{skuId}/stock")
    Boolean updateStock(@PathVariable("skuId") Long skuId, @RequestBody StockReq req);

    /**
     * 批量查询 SKU 信息（含实时价格、库存、上下架状态、规格描述、所属店铺）。 <p>
     * 供购物车、订单等场景一次取齐，避免逐条调用。
     *
     * @param skuIds SKU ID 列表
     * @return SKU 信息列表
     */
    @PostMapping("/batch-query")
    List<SkuResp> listSkusByIds(@RequestBody List<Long> skuIds);
}
