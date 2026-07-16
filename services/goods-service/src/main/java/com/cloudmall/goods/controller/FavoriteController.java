package com.cloudmall.goods.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.FavoriteToggleReq;
import com.cloudmall.goods.service.IFavoriteService;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PutMapping
    public Result<Boolean> toggle(@RequestBody FavoriteToggleReq req) {
        return Result.success(favoriteService.toggle(req.getUserId(), req.getGoodsId()));
    }

    @GetMapping
    public Result<List<Long>> listGoodsIds(@RequestParam Long userId) {
        return Result.success(favoriteService.listGoodsIds(userId));
    }
}
