package com.cloudmall.seckill.service;

import java.math.BigDecimal;

import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;

public interface ISeckillService {
    // Activity CRUD
    Long createActivity(SeckillActivityDO activity);
    ActivityResp getActivity(Long id);
    // Goods submit for seckill
    void submitGoods(Long activityId, Long goodsId, BigDecimal seckillPrice, Integer stock);
    // Audit
    void auditGoods(Long goodsId, boolean approved);
    boolean verifyGoods(Long activityId, Long goodsId);
}