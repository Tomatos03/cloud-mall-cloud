package com.cloudmall.goods.service;

import com.cloudmall.goods.api.response.CommentResp;

import java.util.List;

public interface ICommentService {
    List<CommentResp> listByGoodsId(Long goodsId);
    Long create(Long goodsId, Long userId, String content, Integer rating);
}
