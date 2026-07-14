package com.cloudmall.im.model.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudmall.im.model.chat.entity.ChatSession;
import com.cloudmall.im.model.chat.mapper.ChatSessionMapper;
import com.cloudmall.im.model.chat.service.IChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 会话服务实现
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@Service
@RequiredArgsConstructor
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {
}
