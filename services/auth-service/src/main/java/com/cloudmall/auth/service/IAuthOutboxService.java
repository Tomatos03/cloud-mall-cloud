package com.cloudmall.auth.service;

/**
 * 认证服务 Outbox 服务
 * <p>
 * 在注册事务内写入待发布事件，由定时任务发布到 RocketMQ。
 *
 * @author : Tomatos
 */
public interface IAuthOutboxService {

    /**
     * 追加用户注册事件（与注册事务同提交）。
     *
     * @param userId 用户 ID
     * @param username 用户名
     */
    void appendUserRegistered(Long userId, String username);

    /**
     * 发布一批待发布事件，更新发布状态。
     *
     * @param batchSize 批次大小
     * @return 本次尝试发布条数
     */
    int publishPending(int batchSize);
}
