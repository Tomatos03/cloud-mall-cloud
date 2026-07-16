package com.cloudmall.goods.service;

import java.util.List;

import com.cloudmall.goods.api.response.CommentResp;

public interface ICommentService {
    List<CommentResp> listByGoodsId(Long goodsId);
    Long create(Long goodsId, Long userId, String content, Integer rating);
}