package com.cloudmall.goods.service;

import com.cloudmall.goods.api.response.CommentResponse;

import java.util.List;

public interface ICommentService {
    List<CommentResponse> listByGoodsId(Long goodsId);
    Long create(Long goodsId, Long userId, String content, Integer rating);
}
