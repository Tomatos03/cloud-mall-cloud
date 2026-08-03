package com.cloudmall.user.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.request.FavoriteToggleReq;

/**
 * 收藏 Feign 客户端。
 */
@FeignClient(name = "user-service", contextId = "favoriteClient", path = "/favorites")
public interface FavoriteClient {

    /**
     * 切换当前用户的收藏状态。
     *
     * @param req 商品收藏请求
     * @return true=已收藏 false=已取消
     */
    @PutMapping
    Result<Boolean> toggle(
            @RequestBody
            FavoriteToggleReq req
    );

    /**
     * 查询当前用户收藏的商品 ID 列表。
     *
     * @return 商品 ID 列表
     */
    @GetMapping
    Result<List<Long>> listProductIds();
}
