package com.cloudmall.seckill.service;

import com.cloudmall.seckill.api.response.SeckillActivityResponse;
import com.cloudmall.seckill.entity.SeckillActivityDO;

import java.math.BigDecimal;

public interface ISeckillService {
    // Activity CRUD
    Long createActivity(SeckillActivityDO activity);
    SeckillActivityResponse getActivity(Long id);
    // Goods submit for seckill
    void submitGoods(Long activityId, Long goodsId, BigDecimal seckillPrice, Integer stock);
    // Audit
    void auditGoods(Long goodsId, boolean approved);
    boolean verifyGoods(Long activityId, Long goodsId);
}
