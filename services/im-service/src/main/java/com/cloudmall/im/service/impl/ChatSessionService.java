package com.cloudmall.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.cloudmall.im.mapper.ChatSessionMapper;
import com.cloudmall.im.model.entity.ChatSessionDO;
import com.cloudmall.im.model.req.SessionPageReq;
import com.cloudmall.im.service.IChatSessionService;

/**
 * 聊天会话服务实现
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Service
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSessionDO> implements IChatSessionService {

    @Override
    public ChatSessionDO selectByBuyerAndMerchant(Long buyerId, Long merchantId) {
        return baseMapper.selectByBuyerAndMerchant(buyerId, merchantId);
    }

    @Override
    public IPage<ChatSessionDO> pageByMember(Long currentUserId, SessionPageReq req) {
        return baseMapper.pageByMember(currentUserId, req);
    }

    @Override
    public void updatePinned(ChatSessionDO session, Long userId, boolean pinned) {
        LambdaUpdateWrapper<ChatSessionDO> wrapper = Wrappers.<ChatSessionDO>lambdaUpdate()
                                                             .eq(ChatSessionDO::getId, session.getId())
                                                             .set(
                                                                     session.isBuyer(userId),
                                                                     ChatSessionDO::getBuyerPinned,
                                                                     pinned
                                                             )
                                                             .set(
                                                                     session.isMerchant(userId),
                                                                     ChatSessionDO::getMerchantPinned,
                                                                     pinned
                                                             );
        update(wrapper);
    }

    @Override
    public void updateHidden(ChatSessionDO session, Long userId, boolean hidden) {
        LambdaUpdateWrapper<ChatSessionDO> wrapper = Wrappers.<ChatSessionDO>lambdaUpdate()
                                                             .eq(ChatSessionDO::getId, session.getId())
                                                             .set(
                                                                     session.isBuyer(userId),
                                                                     ChatSessionDO::getBuyerHidden,
                                                                     hidden
                                                             )
                                                             .set(
                                                                     session.isMerchant(userId),
                                                                     ChatSessionDO::getMerchantHidden,
                                                                     hidden
                                                             );
        update(wrapper);
    }
}
