package com.cloudmall.goods.service;

import java.util.List;

import com.cloudmall.goods.api.request.CommentCreateReq;
import com.cloudmall.goods.api.request.CommentReq;
import com.cloudmall.goods.api.response.CommentResp;

public interface ICommentService {
    List<CommentResp> list(CommentReq req);
    Long create(CommentCreateReq req);
}