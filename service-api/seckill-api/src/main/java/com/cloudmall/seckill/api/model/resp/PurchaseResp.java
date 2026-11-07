package com.cloudmall.seckill.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀抢购响应
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResp {

    /** 秒杀单 ID */
    private Long seckillOrderId;

    /** 真实订单号（秒杀侧预生成，消费者侧建单时传入） */
    private String orderNo;

    /** 秒杀单状态 */
    private String status;
}
