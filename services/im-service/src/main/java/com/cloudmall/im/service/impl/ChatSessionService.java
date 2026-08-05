package com.cloudmall.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.cloudmall.im.entity.ChatSessionDO;
import com.cloudmall.im.mapper.ChatSessionMapper;
import com.cloudmall.im.service.IChatSessionService;

/**
 * 聊天会话服务实现
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Service
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSessionDO> implements IChatSessionService {
}
