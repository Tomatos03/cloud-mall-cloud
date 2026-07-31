package com.cloudmall.seckill.service.impl;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.service.ISeckillService;

@Service
@RequiredArgsConstructor
public class SeckillService implements ISeckillService {
    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper productMapper;
    private final SeckillConverter seckillConverter;

    @Override
    public Long createActivity(SeckillActivityDO activity) {
        activity.setStatus("DRAFT");
        activityMapper.insert(activity);
        return activity.getId();
    }

    @Override
    public ActivityResp getActivity(Long id) {
        SeckillActivityDO act = activityMapper.selectById(id);
        AssertUtils.notNull(act, BizErrorCode.DATA_NOT_FOUND);
        return seckillConverter.toResp(act);
    }

    @Override
    @Transactional
    public void submitProduct(Long activityId, Long productId, BigDecimal seckillPrice, Integer stock) {
        SeckillActivityDO act = activityMapper.selectById(activityId);
        AssertUtils.notNull(act, BizErrorCode.DATA_NOT_FOUND);
        SeckillProductDO product = SeckillProductDO.builder()
                .activityId(activityId)
                .productId(productId)
                .seckillPrice(seckillPrice)
                .stock(stock)
                .auditStatus("PENDING")
                .build();
        productMapper.insert(product);
    }

    @Override
    @Transactional
    public void auditProduct(Long productId, boolean approved) {
        SeckillProductDO product = productMapper.selectById(productId);
        AssertUtils.notNull(product, BizErrorCode.DATA_NOT_FOUND);
        product.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        productMapper.updateById(product);
    }

    @Override
    public boolean verifyProduct(Long activityId, Long productId) {
        SeckillProductDO product = productMapper.selectOne(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getProductId, productId)
                        .eq(SeckillProductDO::getAuditStatus, "APPROVED")
        );
        return product != null;
    }
}
