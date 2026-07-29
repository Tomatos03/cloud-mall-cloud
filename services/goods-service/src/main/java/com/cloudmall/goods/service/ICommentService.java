package com.cloudmall.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.goods.api.request.CommentCreateReq;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.model.req.CommentPageReq;

public interface ICommentService {
    IPage<CommentResp> page(CommentPageReq req);
    Long create(CommentCreateReq req);
}