package com.cloudmall.seckill.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.seckill.application.SeckillOrderApp;

/**
 * 秒杀单超时回收 JobHandler
 * <p>
 * 编排逻辑收拢在 {@link SeckillOrderApp#recoverTimeoutOrders()}：
 * 扫描未支付秒杀单 → 条件取消 → 归还 Redis 库存 → 幂等取消真实订单。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class SeckillOrderTimeoutRecoveryJobHandler {

    private final SeckillOrderApp seckillOrderApp;

    /**
     * 每分钟执行一次超时回收。
     *
     * @param param 任务参数（未使用）
     */
    @XxlJob("seckillOrderTimeoutRecovery")
    public void recover(String param) {
        seckillOrderApp.recoverTimeoutOrders();
    }
}
