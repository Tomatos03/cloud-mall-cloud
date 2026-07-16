package com.cloudmall.im.model.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.im.model.chat.entity.ChatSession;

/**
 * 会话Mapper接口
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}