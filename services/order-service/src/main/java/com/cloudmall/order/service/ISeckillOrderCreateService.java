package com.cloudmall.order.service;

import com.cloudmall.order.api.request.SeckillOrderCreateReq;
import com.cloudmall.order.api.response.OrderResp;

/**
 * 秒杀订单建单服务
 *
 * @author : Tomatos
 */
public interface ISeckillOrderCreateService {

    /**
     * 创建秒杀订单（跳过购物车/优惠券/普通库存预占，按订单号幂等）。
     *
     * @param req 建单请求
     * @return 订单响应
     */
    OrderResp createSeckillOrder(SeckillOrderCreateReq req);
}
