package com.cloudmall.feign.exception;

/**
 * 远程调用失败类型
 * <p>
 * 用于区分远端业务错误、传输故障、超时、限流等失败场景，供异常分类与全局错误映射使用。
 *
 * @author : Tomatos
 */
public enum RemoteErrorTypeEnum {
    /** 远端返回业务错误码 */
    BUSINESS,

    /** 连接失败、无可用实例等传输故障 */
    TRANSPORT,

    /** 连接或读取超时 */
    TIMEOUT,

    /** 远端返回 5xx */
    SERVER,

    /** 远端返回 404 */
    NOT_FOUND,

    /** 限流或熔断拦截 */
    REJECTED,

    /** 响应无法解析 */
    DECODE
}
