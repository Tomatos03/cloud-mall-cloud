package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping("/toggle")
    public Result<Boolean> toggle(@RequestParam Long userId, @RequestParam Long goodsId) {
        return Result.success(favoriteService.toggle(userId, goodsId));
    }

    @GetMapping("/list/{userId}")
    public Result<List<Long>> listGoodsIds(@PathVariable Long userId) {
        return Result.success(favoriteService.listGoodsIds(userId));
    }
}
