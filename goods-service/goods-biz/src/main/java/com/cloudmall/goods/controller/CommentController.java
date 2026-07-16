package com.cloudmall.goods.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.CommentCreateReq;
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

    @GetMapping
    public Result<List<CommentResp>> listByGoodsId(@RequestParam Long goodsId) {
        return Result.success(commentService.listByGoodsId(goodsId));
    }

    @PostMapping
    public Result<Long> create(@RequestBody CommentCreateReq req) {
        return Result.success(commentService.create(req.getGoodsId(), req.getUserId(),
                req.getContent(), req.getRating()));
    }
}
