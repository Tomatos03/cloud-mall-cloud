package com.cloudmall.im.model.bo;

/**
 * 用户连接路由元数据。
 *
 * @param instanceId 连接所在实例 ID
 * @param sessionId WebSocket 会话 ID
 * @param lastHeartbeat 最近心跳时间戳（毫秒）
 * @author : Tomatos
 * @date : 2026/11/25
 */
public record UserRoute(String instanceId, String sessionId, long lastHeartbeat) {
}
