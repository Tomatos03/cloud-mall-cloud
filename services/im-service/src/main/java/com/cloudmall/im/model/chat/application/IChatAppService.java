package com.cloudmall.im.model.chat.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.im.common.entity.PageParamsDTO;
import com.cloudmall.im.model.chat.dto.CreateSessionResult;
import com.cloudmall.im.model.chat.vo.ChatSessionVO;

/**
 * 聊天应用服务接口
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
public interface IChatAppService {

    IPage<ChatSessionVO> pageChatSession(PageParamsDTO paramsDTO);

    void markReadForChatSession(Long sessionId);

    CreateSessionResult createChatSession(Long storeId);
}
