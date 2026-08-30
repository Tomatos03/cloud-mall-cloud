package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 收藏 Feign 客户端。
 */
@FeignClient(name = "user-service", contextId = "favoriteClient")
public interface FavoriteClient {

    /**
     * 收藏指定商品（幂等）。
     *
     * @param productId 商品 ID
     * @return 处理结果
     */
    @PostMapping("/products/{productId}/favorite")
    Void favorite(@PathVariable Long productId);

    /**
     * 取消收藏指定商品（幂等）。
     *
     * @param productId 商品 ID
     * @return 处理结果
     */
    @DeleteMapping("/products/{productId}/favorite")
    Void unfavorite(@PathVariable Long productId);

    /**
     * 查询当前用户收藏的商品 ID 列表。
     *
     * @return 商品 ID 列表
     */
    @GetMapping("/favorites")
    List<Long> listProductIds();
}
