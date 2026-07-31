package com.cloudmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.CommentCreateReq;
import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.model.req.CommentPageReq;
import com.cloudmall.product.service.ICommentService;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * 分页查询商品评论。
     *
     * @param req 分页查询条件
     * @return 评论分页结果
     */
    @GetMapping
    public Result<IPage<CommentResp>> page(CommentPageReq req) {
        return Result.success(commentService.page(req));
    }

    /**
     * 创建商品评论。
     *
     * @param req 评论创建请求
     * @return 评论ID
     */
    @PostMapping
    public Result<Void> create(@RequestBody @Valid CommentCreateReq req) {
        commentService.create(req);
        return Result.success();
    }

}
