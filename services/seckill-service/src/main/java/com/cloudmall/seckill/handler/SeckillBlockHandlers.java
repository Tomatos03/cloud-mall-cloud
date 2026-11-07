package com.cloudmall.seckill.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.seckill.api.model.req.SeckillPurchaseReq;
import com.cloudmall.seckill.api.model.resp.PurchaseResp;

/**
 * 秒杀服务 Sentinel 兜底处理器
 * <p>
 * 集中管理 @SentinelResource 的 blockHandler 静态方法，被限流或熔断时快速失败
 * 并抛出统一业务异常。blockHandler 放在外部类时方法必须为 static。
 *
 * @author : Tomatos
 */
public final class SeckillBlockHandlers {

    private SeckillBlockHandlers() {}

    /**
     * 抢购被限流拦截时的兜底。
     *
     * @param req 抢购请求
     * @param ex 限流异常
     * @return 不会返回，直接抛业务异常
     */
    public static PurchaseResp purchaseBlock(SeckillPurchaseReq req, BlockException ex) {
        throw new BizException(BizErrorEnum.FLOW_CONTROL_BLOCKED);
    }
}
