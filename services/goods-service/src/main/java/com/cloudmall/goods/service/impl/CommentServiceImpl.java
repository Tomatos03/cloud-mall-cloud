package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.entity.CommentDO;
import com.cloudmall.goods.mapper.CommentMapper;
import com.cloudmall.goods.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final CommentMapper commentMapper;

    @Override
    public List<CommentResp> listByGoodsId(Long goodsId) {
        List<CommentDO> list = commentMapper.selectList(
                new LambdaQueryWrapper<CommentDO>()
                        .eq(CommentDO::getGoodsId, goodsId)
                        .eq(CommentDO::getStatus, 1)
                        .orderByDesc(CommentDO::getCreateTime)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Long create(Long goodsId, Long userId, String content, Integer rating) {
        CommentDO c = new CommentDO();
        c.setGoodsId(goodsId);
        c.setUserId(userId);
        c.setContent(content);
        c.setRating(rating != null ? rating : 5);
        c.setStatus(1);
        c.setCreateTime(LocalDateTime.now());
        commentMapper.insert(c);
        return c.getId();
    }

    private CommentResp toResponse(CommentDO c) {
        CommentResp r = new CommentResp();
        r.setId(c.getId());
        r.setUserId(c.getUserId());
        r.setContent(c.getContent());
        r.setRating(c.getRating());
        r.setCreateTime(c.getCreateTime());
        return r;
    }
}
