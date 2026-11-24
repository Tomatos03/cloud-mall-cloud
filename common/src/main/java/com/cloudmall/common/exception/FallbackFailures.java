package com.cloudmall.common.exception;

import com.cloudmall.common.enums.BizErrorEnum;

/**
 * 降级工厂快速失败工具
 * <p>
 * 核心写方法与关键读方法不允许返回空值降级，否则会造成数据不一致。本工具优先原样重抛运行时异常以保留
 * 远程调用错误码；Sentinel 的 BlockException 属于受检异常，按业务码映射为 FLOW_CONTROL_BLOCKED。
 *
 * @author : Tomatos
 */
public final class FallbackFailures {

    private static final String SENTINEL_BLOCK_PACKAGE = "com.alibaba.csp.sentinel.slots.block.";

    private FallbackFailures() {}

    /**
     * 构造 fallback 中应抛出的异常。
     *
     * @param cause 原始失败原因
     * @param fallbackError 无法保留原始异常时使用的业务错误码
     * @return 应抛出的运行时异常
     */
    public static RuntimeException of(Throwable cause, BizErrorEnum fallbackError) {
        if (cause instanceof RuntimeException runtime) {
            return runtime;
        }

        if (cause != null && cause.getClass()
                                  .getName()
                                  .startsWith(SENTINEL_BLOCK_PACKAGE)) {
            return new BizException(BizErrorEnum.FLOW_CONTROL_BLOCKED);
        }

        return new BizException(fallbackError);
    }
}
