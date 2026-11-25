package com.cloudmall.im.infrastructure;

import com.cloudmall.im.api.model.event.ImMessagePushEvent;

/**
 * 推送路由策略。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IPushDispatcher {

    void dispatch(ImMessagePushEvent event);
}
