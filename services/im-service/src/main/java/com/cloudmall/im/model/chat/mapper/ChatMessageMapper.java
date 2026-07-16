package com.cloudmall.im.model.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.im.model.chat.entity.ChatMessage;

/**
 * 消息Mapper接口
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}