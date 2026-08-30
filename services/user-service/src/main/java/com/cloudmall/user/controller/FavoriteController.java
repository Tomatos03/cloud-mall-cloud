package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.service.IFavoriteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final IFavoriteService favoriteService;

    /**
     * 查询当前用户收藏的商品 ID 列表。
     *
     * @return 商品 ID 列表
     */
    @GetMapping
    public Result<List<Long>> listProductIds() {
        return Result.success(favoriteService.listProductIds(UserContextHolder.getUserId()));
    }

    /**
     * 查询当前用户是否已收藏指定商品。
     *
     * @param productId 商品 ID
     * @return true=已收藏 false=未收藏
     */
    @GetMapping("/{productId}")
    public Result<Boolean> isFavorited(@PathVariable Long productId) {
        return Result.success(favoriteService.isFavorited(UserContextHolder.getUserId(), productId));
    }

    /**
     * 收藏指定商品（幂等）。
     *
     * @param productId 商品 ID
     * @return 处理结果
     */
    @PostMapping("/{productId}")
    public Result<Void> favorite(@PathVariable Long productId) {
        favoriteService.favorite(UserContextHolder.getUserId(), productId);
        return Result.success();
    }

    /**
     * 取消收藏指定商品（幂等）。
     *
     * @param productId 商品 ID
     * @return 处理结果
     */
    @DeleteMapping("/{productId}")
    public Result<Void> unfavorite(@PathVariable Long productId) {
        favoriteService.unfavorite(UserContextHolder.getUserId(), productId);
        return Result.success();
    }
}
