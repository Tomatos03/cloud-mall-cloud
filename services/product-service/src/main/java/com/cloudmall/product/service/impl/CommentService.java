package com.cloudmall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.product.api.request.CommentCreateReq;
import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.convert.CommentConverter;
import com.cloudmall.product.entity.CommentDO;
import com.cloudmall.product.mapper.CommentMapper;
import com.cloudmall.product.model.req.CommentPageReq;
import com.cloudmall.product.service.ICommentService;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentMapper commentMapper;
    private final CommentConverter commentConverter;

    @Override
    public IPage<CommentResp> page(CommentPageReq req) {
        return commentMapper.selectPage(req, req.buildLambdaWrapper())
                            .convert(commentConverter::toResp);
    }

    @Override
    public void create(CommentCreateReq req) {
        Long userId = UserContextHolder.getUserId();
        AssertUtils.notNull(userId, BizErrorCode.USER_NOT_AUTHENTICATED);
        CommentDO comment = commentConverter.toDO(req, userId);
        commentMapper.insert(comment);
    }

}
