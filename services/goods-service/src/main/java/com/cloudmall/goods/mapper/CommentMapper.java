package com.cloudmall.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.goods.entity.CommentDO;

@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {
}