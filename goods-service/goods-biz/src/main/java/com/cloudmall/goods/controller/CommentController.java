package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @GetMapping("/list/{goodsId}")
    public Result<List<CommentResp>> listByGoodsId(@PathVariable Long goodsId) {
        return Result.success(commentService.listByGoodsId(goodsId));
    }

    @PostMapping
    public Result<Long> create(@RequestParam Long goodsId, @RequestParam Long userId,
                               @RequestParam String content, @RequestParam(required = false) Integer rating) {
        return Result.success(commentService.create(goodsId, userId, content, rating));
    }
}
