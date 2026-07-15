package com.cloudmall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillGoodsDO;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillGoodsMapper;
import com.cloudmall.seckill.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements ISeckillService {
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
        if (act == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        ActivityResp r = new ActivityResp();
        r.setId(act.getId());
        r.setName(act.getName());
        r.setStartTime(act.getStartTime());
        r.setEndTime(act.getEndTime());
        r.setStatus(act.getStatus());
        return r;
    }

    @Override
    @Transactional
    public void submitGoods(Long activityId, Long goodsId, BigDecimal seckillPrice, Integer stock) {
        SeckillActivityDO act = activityMapper.selectById(activityId);
        if (act == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        SeckillGoodsDO g = new SeckillGoodsDO();
        g.setActivityId(activityId);
        g.setGoodsId(goodsId);
        g.setSeckillPrice(seckillPrice);
        g.setStock(stock);
        g.setAuditStatus("PENDING");
        goodsMapper.insert(g);
    }

    @Override
    @Transactional
    public void auditGoods(Long goodsId, boolean approved) {
        SeckillGoodsDO g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        g.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        goodsMapper.updateById(g);
    }

    @Override
    public boolean verifyGoods(Long activityId, Long goodsId) {
        SeckillGoodsDO g = goodsMapper.selectOne(
                new LambdaQueryWrapper<SeckillGoodsDO>()
                        .eq(SeckillGoodsDO::getActivityId, activityId)
                        .eq(SeckillGoodsDO::getGoodsId, goodsId)
                        .eq(SeckillGoodsDO::getAuditStatus, "APPROVED")
        );
        return g != null;
    }
}
