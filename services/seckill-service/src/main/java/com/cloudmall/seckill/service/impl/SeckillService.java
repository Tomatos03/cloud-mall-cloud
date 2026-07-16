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
import com.cloudmall.seckill.entity.SeckillGoodsDO;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillGoodsMapper;
import com.cloudmall.seckill.service.ISeckillService;

@Service
@RequiredArgsConstructor
public class SeckillService implements ISeckillService {
    private final SeckillActivityMapper activityMapper;
    private final SeckillGoodsMapper goodsMapper;

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
        ActivityResp r = ActivityResp.builder()
                .id(act.getId())
                .name(act.getName())
                .startTime(act.getStartTime())
                .endTime(act.getEndTime())
                .status(act.getStatus())
                .build();
        return r;
    }

    @Override
    @Transactional
    public void submitGoods(Long activityId, Long goodsId, BigDecimal seckillPrice, Integer stock) {
        SeckillActivityDO act = activityMapper.selectById(activityId);
        AssertUtils.notNull(act, BizErrorCode.DATA_NOT_FOUND);
        SeckillGoodsDO g = SeckillGoodsDO.builder()
                .activityId(activityId)
                .goodsId(goodsId)
                .seckillPrice(seckillPrice)
                .stock(stock)
                .auditStatus("PENDING")
                .build();
        goodsMapper.insert(g);
    }

    @Override
    @Transactional
    public void auditGoods(Long goodsId, boolean approved) {
        SeckillGoodsDO g = goodsMapper.selectById(goodsId);
        AssertUtils.notNull(g, BizErrorCode.DATA_NOT_FOUND);
        g.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        goodsMapper.updateById(g);
    }

    @Override
    public boolean verifyGoods(Long activityId, Long goodsId) {
        SeckillGoodsDO g = goodsMapper.selectOne(
                Wrappers.<SeckillGoodsDO>lambdaQuery()
                        .eq(SeckillGoodsDO::getActivityId, activityId)
                        .eq(SeckillGoodsDO::getGoodsId, goodsId)
                        .eq(SeckillGoodsDO::getAuditStatus, "APPROVED")
        );
        return g != null;
    }
}
