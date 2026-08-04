package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.request.FavoriteToggleReq;
import com.cloudmall.user.service.IFavoriteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final IFavoriteService favoriteService;

    /**
     * 切换当前用户对某商品的收藏状态。
     *
     * @param req 商品收藏请求
     * @return true=已收藏 false=已取消
     */
    @PutMapping
    public Result<Boolean> toggle(
            @RequestBody
            FavoriteToggleReq req
    ) {
        return Result.success(favoriteService.toggle(UserContextHolder.getUserId(), req.getProductId()));
    }

    /**
     * 查询当前用户收藏的商品 ID 列表。
     *
     * @return 商品 ID 列表
     */
    @GetMapping
    public Result<List<Long>> listProductIds() {
        return Result.success(favoriteService.listProductIds(UserContextHolder.getUserId()));
    }
}
