package com.cloudmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.product.api.request.CommentCreateReq;
import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.model.req.CommentPageReq;

public interface ICommentService {

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    IPage<CommentResp> page(CommentPageReq req);

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     */
    void create(CommentCreateReq req);
}
