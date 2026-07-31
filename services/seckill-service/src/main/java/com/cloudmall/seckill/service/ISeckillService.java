package com.cloudmall.seckill.service;

import java.math.BigDecimal;

import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;

public interface ISeckillService {
    // Activity CRUD

    /**
     * 创建并保存数据。
     *
     * @param activity 秒杀活动实体
     * @return 结果标识或数值
     */
    Long createActivity(SeckillActivityDO activity);

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     */
    ActivityResp getActivity(Long id);
    // Product submit for seckill

    /**
     * 提交商品参加秒杀活动。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @param seckillPrice 秒杀价格
     * @param stock 库存数量
     */
    void submitProduct(Long activityId, Long productId, BigDecimal seckillPrice, Integer stock);
    // Audit

    /**
     * 审核秒杀活动商品。
     *
     * @param productId 商品标识
     * @param approved 是否通过审核
     */
    void auditProduct(Long productId, boolean approved);

    /**
     * 校验相关数据。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @return 是否满足条件
     */
    boolean verifyProduct(Long activityId, Long productId);
}
