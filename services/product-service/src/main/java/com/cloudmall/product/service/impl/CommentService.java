package com.cloudmall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
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

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<CommentResp> page(CommentPageReq req) {
        IPage<CommentDO> page = commentMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(commentConverter::toResp);
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public void create(CommentCreateReq req) {
        Long userId = Asserts.requireNonNull(UserContextHolder.getUserId(), BizError.USER_NOT_AUTHENTICATED);
        CommentDO comment = commentConverter.toDO(req, userId);
        commentMapper.insert(comment);
    }

}
