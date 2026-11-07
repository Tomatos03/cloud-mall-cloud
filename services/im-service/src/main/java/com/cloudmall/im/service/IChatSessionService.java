package com.cloudmall.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.cloudmall.im.model.entity.ChatSessionDO;
import com.cloudmall.im.model.req.SessionPageReq;

/**
 * 聊天会话服务接口
 *
 * @author : Tomatos
 * @date : 2026/8
 */
public interface IChatSessionService extends IService<ChatSessionDO> {

    /**
     * 根据买家与商家用户查询会话。
     *
     * @param buyerId 买家用户ID
     * @param merchantId 商家用户ID
     * @return 会话实体，不存在时返回 null
     */
    ChatSessionDO selectByBuyerAndMerchant(Long buyerId, Long merchantId);

    /**
     * 分页查询当前用户参与的会话，按创建时间倒序。
     *
     * @param currentUserId 当前用户ID
     * @param req 分页请求
     * @return 会话分页数据
     */
    IPage<ChatSessionDO> pageByMember(Long currentUserId, SessionPageReq req);
}
