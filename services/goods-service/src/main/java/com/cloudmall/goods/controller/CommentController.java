package com.cloudmall.goods.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.request.CommentCreateReq;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.service.ICommentService;

@RestController
@RequestMapping("/comments")
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
