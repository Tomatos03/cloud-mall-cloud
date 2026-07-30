package com.cloudmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.request.CommentCreateReq;
import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.model.req.CommentPageReq;

public interface ICommentService {
    IPage<CommentResp> page(CommentPageReq req);
    Long create(CommentCreateReq req);
}