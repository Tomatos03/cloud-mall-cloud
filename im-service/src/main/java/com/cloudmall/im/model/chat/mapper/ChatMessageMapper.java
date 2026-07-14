package com.cloudmall.im.model.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.im.model.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper接口
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
