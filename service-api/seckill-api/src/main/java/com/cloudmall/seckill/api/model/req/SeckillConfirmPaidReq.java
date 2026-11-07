package com.cloudmall.seckill.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀支付确认请求
 * <p>
 * consumer-service 消费 OrderPaid 事件后回调秒杀侧，按真实订单号定位秒杀单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillConfirmPaidReq {

    /** 真实订单号 */
    private String orderNo;
}
