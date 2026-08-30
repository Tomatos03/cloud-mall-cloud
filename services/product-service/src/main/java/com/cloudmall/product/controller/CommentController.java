package com.cloudmall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.request.CommentCreateReq;
import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.api.response.CommentSummaryResp;
import com.cloudmall.product.application.CommentApp;
import com.cloudmall.product.model.req.CommentPageReq;
import com.cloudmall.product.service.ICommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentApp commentApp;
    private final ICommentService commentService;

    /**
     * 查询商品评论总体统计。
     *
     * @param productId 商品 ID
     * @return 评论统计
     */
    @GetMapping("/summary")
    public Result<CommentSummaryResp> summary(@RequestParam Long productId) {
        return Result.success(commentApp.getSummary(productId));
    }

    /**
     * 分页查询商品评论（含筛选与排序）。
     *
     * @param req 分页查询条件
     * @return 评论分页结果
     */
    @GetMapping
    public Result<IPage<CommentResp>> page(CommentPageReq req) {
        return Result.success(commentApp.page(req));
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
