package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.FavoriteToggleReq;
import com.cloudmall.goods.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
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
