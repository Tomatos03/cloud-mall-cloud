package com.cloudmall.im.infrastructure.store;

import java.util.List;

import com.cloudmall.im.model.bo.UserRoute;

/**
 * 在线路由存储。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface IPresenceStore {

    void register(Long userId, UserRoute route);

    void refresh(Long userId, UserRoute route);

    List<UserRoute> routes(Long userId);

    void unregisterIfMatch(Long userId, UserRoute route);
}
