package com.cloudmall.im.infrastructure;

/**
 * IM Redis key 常量。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public interface ImRedisKeys {

    /** 连接路由 */
    String ROUTE_PREFIX = "im:route:";

    /** 推送幂等标记 */
    String PUSH_DONE_PREFIX = "im:push:done:";
}
