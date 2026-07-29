package com.cloudmall.goods.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.goods.api.request.CommentCreateReq;
import com.cloudmall.goods.api.request.CommentReq;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.entity.CommentDO;
import com.cloudmall.goods.convert.CommentConverter;
import com.cloudmall.goods.mapper.CommentMapper;
import com.cloudmall.goods.service.ICommentService;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentMapper commentMapper;
    private final CommentConverter commentConverter;

    @Override
    public List<CommentResp> list(CommentReq req) {
        List<CommentDO> list = commentMapper.selectList(
                Wrappers.<CommentDO>lambdaQuery()
                        .eq(CommentDO::getGoodsId, req.getGoodsId())
                        .eq(CommentDO::getStatus, StatusEnum.ENABLED)
                        .orderByDesc(CommentDO::getCreateTime)
        );
        return list.stream()
                   .map(commentConverter::toResp)
                   .toList();
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
