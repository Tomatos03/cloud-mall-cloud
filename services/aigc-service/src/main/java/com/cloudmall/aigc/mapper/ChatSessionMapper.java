package com.cloudmall.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.aigc.entity.ChatSessionDO;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {
}
