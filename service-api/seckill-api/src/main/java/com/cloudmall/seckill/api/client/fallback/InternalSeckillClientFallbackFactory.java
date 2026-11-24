package com.cloudmall.seckill.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.seckill.api.client.InternalSeckillClient;
import com.cloudmall.seckill.api.model.req.SeckillConfirmPaidReq;
import com.cloudmall.seckill.api.model.resp.SeckillOrderResp;

/**
 * 秒杀内部客户端兜底工厂
 * <p>
 * 支付确认与商品审核校验均为核心操作，失败必须快速失败。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalSeckillClientFallbackFactory implements FallbackFactory<InternalSeckillClient> {

    @Override
    public InternalSeckillClient create(Throwable cause) {
        log.warn("秒杀内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalSeckillClient() {
            @Override
            public void confirmPaid(SeckillConfirmPaidReq req) {
                throw failure;
            }

            @Override
            public SeckillOrderResp getByOrderNo(String orderNo) {
                throw failure;
            }

            @Override
            public Boolean verifyProduct(Long activityId, Long productId) {
                throw failure;
            }
        };
    }
}
