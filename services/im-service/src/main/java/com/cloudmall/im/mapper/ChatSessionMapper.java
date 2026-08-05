package com.cloudmall.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.im.entity.ChatSessionDO;

/**
 * 聊天会话 Mapper
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {
}
