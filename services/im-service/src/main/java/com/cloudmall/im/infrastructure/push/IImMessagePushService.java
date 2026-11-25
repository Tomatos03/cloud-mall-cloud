package com.cloudmall.im.infrastructure.push;

import com.cloudmall.im.api.model.resp.MessageResp;

/**
 * 本地 WebSocket 推送服务。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IImMessagePushService {

    boolean pushToLocal(Long receiverId, MessageResp message);
}
