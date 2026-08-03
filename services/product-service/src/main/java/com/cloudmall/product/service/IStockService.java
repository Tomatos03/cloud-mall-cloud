package com.cloudmall.product.service;

import com.cloudmall.product.api.request.StockConfirmReq;
import com.cloudmall.product.api.request.StockOpReq;

/**
 * 普通商品库存服务
 * <p>
 * 为订单创建提供库存预占、支付确认、取消/超时释放和补偿兜底。
 * 同一订单号同一 SKU 同一操作类型幂等，条件更新防止库存扣成负数。
 * 秒杀库存不接入本服务。
 *
 * @author : Tomatos
 */
public interface IStockService {

    /**
     * 按订单释放全部预占库存（取消/超时/补偿场景）。
     *
     * @param orderNo 订单号
     */
    void releaseByOrder(String orderNo);

    /**
     * 预占库存：扣减可售库存并记录预占流水（订单号 + SKU + RESERVE 幂等）。
     *
     * @param req 预占请求
     */
    void reserve(StockOpReq req);

    /**
     * 支付确认：不改库存，记录确认流水（订单号幂等）。
     *
     * @param req 确认请求
     */
    void confirm(StockConfirmReq req);

    /**
     * 释放库存：回补预占数量并记录释放流水（订单号 + SKU + RELEASE 幂等）。
     *
     * @param req 释放请求
     */
    void release(StockOpReq req);

    /**
     * 补偿释放：恢复任务兜底回补并记录补偿流水（订单号 + SKU + COMPENSATE 幂等）。
     *
     * @param req 补偿请求
     */
    void compensate(StockOpReq req);
}
