package com.cloudmall.product.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.product.api.response.ProductDocResp;
import com.cloudmall.product.api.response.ProductResp;

@FeignClient(name = "product-service", contextId = "productClient", path = "/products")
public interface ProductClient {

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    @GetMapping("/{id}")
    ProductResp getById(@PathVariable("id") Long id);

    /**
     * 获取相关数据。
     *
     * @param spu SPU 编码
     * @return 处理结果
     */
    @GetMapping("/spu/{spu}")
    ProductResp getBySpu(@PathVariable("spu") String spu);

    /**
     * 游标拉取商品搜索文档（供 search-service 全量重建 / 定时增量同步使用）。
     *
     * @param req 游标分页参数（cursor / cursorTime 二选一，见
     *            {@link CursorReq}）
     * @return 游标分页结果
     */
    @GetMapping("/docs")
    CursorResult<ProductDocResp> cursorPage(@SpringQueryMap CursorReq req);

    /**
     * 按商品 ID 拉取单个搜索文档（供 search-service MQ 增量消费使用）。
     *
     * @param id 商品 ID
     * @return 搜索文档，商品不存在返回 null
     */
    @GetMapping("/docs/{id}")
    ProductDocResp searchDoc(@PathVariable("id") Long id);
}
