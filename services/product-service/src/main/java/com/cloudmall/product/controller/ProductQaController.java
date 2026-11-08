package com.cloudmall.product.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.model.resp.ProductQaResp;
import com.cloudmall.product.service.IProductQaService;

/**
 * 商品问答 Controller。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductQaController {

    private final IProductQaService productQaService;

    /**
     * 查询商品问答列表。
     *
     * @param productId 商品 SPU ID
     * @param limit 返回条数上限
     * @return 问答列表
     */
    @GetMapping("/{productId}/qas")
    public Result<List<ProductQaResp>> list(
        @PathVariable
        Long productId,
        @RequestParam(value = "limit", defaultValue = "5")
        Integer limit
    ) {
        return Result.success(productQaService.listByProduct(productId, limit));
    }
}
