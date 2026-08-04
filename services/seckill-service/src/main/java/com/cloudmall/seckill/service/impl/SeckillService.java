package com.cloudmall.seckill.service.impl;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.service.ISeckillService;

@Service
@RequiredArgsConstructor
public class SeckillService implements ISeckillService {
    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper productMapper;
    private final SeckillConverter seckillConverter;

    /**
     * 创建并保存数据。
     *
     * @param activity 秒杀活动实体
     * @return 结果标识或数值
     */
    @Override
    public Long createActivity(SeckillActivityDO activity) {
        activity.setStatus("DRAFT");
        activityMapper.insert(activity);
        return activity.getId();
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public ActivityResp getActivity(Long id) {
        SeckillActivityDO act = Asserts.requireNonNull(activityMapper.selectById(id));
        return seckillConverter.toResp(act);
    }

    /**
     * 提交商品参加秒杀活动。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @param seckillPrice 秒杀价格
     * @param stock 库存数量
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitProduct(Long activityId, Long productId, BigDecimal seckillPrice, Integer stock) {
        SeckillActivityDO act = Asserts.requireNonNull(activityMapper.selectById(activityId));
        SeckillProductDO product = SeckillProductDO.builder()
                                                   .activityId(activityId)
                                                   .productId(productId)
                                                   .seckillPrice(seckillPrice)
                                                   .stock(stock)
                                                   .auditStatus("PENDING")
                                                   .build();
        productMapper.insert(product);
    }

    /**
     * 审核秒杀活动商品。
     *
     * @param productId 商品标识
     * @param approved 是否通过审核
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditProduct(Long productId, boolean approved) {
        SeckillProductDO product = Asserts.requireNonNull(productMapper.selectById(productId));
        product.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        productMapper.updateById(product);
    }

    /**
     * 校验相关数据。
     *
     * @param activityId 活动标识
     * @param productId 商品标识
     * @return 是否满足条件
     */
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
