package com.cloudmall.goods.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public List<CommentResp> listByGoodsId(Long goodsId) {
        List<CommentDO> list = commentMapper.selectList(
                Wrappers.<CommentDO>lambdaQuery()
                        .eq(CommentDO::getGoodsId, goodsId)
                        .eq(CommentDO::getStatus, StatusEnum.ENABLED)
                        .orderByDesc(CommentDO::getCreateTime)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Long create(Long goodsId, Long userId, String content, Integer rating) {
        CommentDO c = CommentDO.builder()
                .goodsId(goodsId)
                .userId(userId)
                .content(content)
                .rating(rating != null ? rating : 5)
                .status(StatusEnum.ENABLED)
                .createTime(LocalDateTime.now())
                .build();
        commentMapper.insert(c);
        return c.getId();
    }

    private CommentResp toResponse(CommentDO c) {
        return commentConverter.toResp(c);
    }
}
