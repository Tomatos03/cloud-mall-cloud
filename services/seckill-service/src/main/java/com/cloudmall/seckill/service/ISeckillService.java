package com.cloudmall.seckill.service;

import java.math.BigDecimal;

import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;

public interface ISeckillService {
    // Activity CRUD
    Long createActivity(SeckillActivityDO activity);
    ActivityResp getActivity(Long id);
    // Product submit for seckill
    void submitProduct(Long activityId, Long productId, BigDecimal seckillPrice, Integer stock);
    // Audit
    void auditProduct(Long productId, boolean approved);
    boolean verifyProduct(Long activityId, Long productId);
}
