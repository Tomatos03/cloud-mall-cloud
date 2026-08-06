package com.cloudmall.seckill.service;

import java.util.List;

import com.cloudmall.seckill.api.response.SeckillOrderResp;
import com.cloudmall.seckill.entity.SeckillOrderDO;

/**
 * 秒杀单服务
 *
 * @author : Tomatos
 */
public interface ISeckillOrderService {

    /**
     * 落库秒杀单并同事务追加建单事件。 <p>
     * 唯一键 uk_activity_sku_user 为一人一单的权威保证。
     *
     * @param order 秒杀单实体
     * @param addressId 收货地址 ID（进事件负载，不落秒杀单）
     */
    void createWithEvent(SeckillOrderDO order, Long addressId);

    /**
     * 支付确认：PENDING_PAYMENT → PAID 并落库扣减秒杀池。
     *
     * @param orderNo 真实订单号
     */
    void confirmPaid(String orderNo);

    /**
     * 扫描超时未支付的秒杀单。
     *
     * @return 待回收的秒杀单列表
     */
    List<SeckillOrderDO> scanTimeoutOrders();

    /**
     * 条件取消秒杀单：PENDING_PAYMENT → CANCELLED。
     *
     * @param seckillOrderId 秒杀单 ID
     * @return true 表示本次调用成功取消，调用方才可归还库存
     */
    boolean cancelIfPending(Long seckillOrderId);

    /**
     * 按真实订单号查询秒杀单。
     *
     * @param orderNo 真实订单号
     * @return 秒杀单实体
     */
    SeckillOrderDO getByOrderNo(String orderNo);

    /**
     * 查询用户的秒杀单（按 ID 倒序）。
     *
     * @param userId 用户 ID
     * @return 秒杀单列表
     */
    List<SeckillOrderResp> listByUserId(Long userId);
}
