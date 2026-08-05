package com.cloudmall.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.im.entity.ChatMessageDO;

/**
 * 聊天消息 Mapper
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageDO> {
}
