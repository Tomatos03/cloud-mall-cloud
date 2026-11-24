package com.cloudmall.product.service;

import com.cloudmall.common.model.page.PageResp;
import com.cloudmall.product.api.model.req.CommentCreateReq;
import com.cloudmall.product.api.model.req.CommentPageReq;
import com.cloudmall.product.api.model.resp.CommentResp;

public interface ICommentService {

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    PageResp<CommentResp> page(CommentPageReq req);

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     */
    void create(CommentCreateReq req);
}
