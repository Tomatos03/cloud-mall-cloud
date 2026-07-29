package com.cloudmall.goods.service.impl;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.api.request.CommentCreateReq;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.convert.CommentConverter;
import com.cloudmall.goods.entity.CommentDO;
import com.cloudmall.goods.mapper.CommentMapper;
import com.cloudmall.goods.model.req.CommentPageReq;
import com.cloudmall.goods.service.ICommentService;
import com.cloudmall.mybatisplus.enums.StatusEnum;

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
    public Long create(CommentCreateReq req) {
        CommentDO c = CommentDO.builder()
                .goodsId(req.getGoodsId())
                .userId(req.getUserId())
                .content(req.getContent())
                .rating(req.getRating() != null ? req.getRating() : 5)
                .status(StatusEnum.ENABLED)
                .createTime(LocalDateTime.now())
                .build();
        commentMapper.insert(c);
        return c.getId();
    }

}
