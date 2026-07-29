package com.cloudmall.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.goods.api.request.CommentCreateReq;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.model.req.CommentPageReq;
import com.cloudmall.goods.service.ICommentService;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @GetMapping
    public Result<IPage<CommentResp>> page(CommentPageReq req) {
        return Result.success(commentService.page(req));
    }

    @PostMapping
    public Result<Long> create(@RequestBody CommentCreateReq req) {
        return Result.success(commentService.create(req));
    }

}
