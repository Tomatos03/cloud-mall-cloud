package com.cloudmall.seckill.job;

import java.time.LocalDateTime;
import java.util.List;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.seckill.cache.SeckillCacheService;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.model.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.entity.SeckillProductDO;

/**
 * 秒杀缓存预热 JobHandler
 * <p>
 * 仅在活动开始后、结束前（报名截止、商家禁止新增商品）为活动预热库存 key
 * （SET NX 不覆盖运行时值），并清理已结束/已取消活动的库存 key。
 * 这样可避免预热后商家继续报名或调整商品导致缓存与数据库不一致。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillCachePreheatJobHandler {

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper productMapper;
    private final SeckillCacheService cacheService;

    /**
     * 每分钟执行一次预热与清理。
     *
     * @param param 任务参数（未使用）
     */
    @XxlJob("seckillCachePreheat")
    public void preheat(String param) {
        try {
            preheatStarted();
            cleanFinished();
        } catch (Exception e) {
            log.error("秒杀缓存预热失败", e);
        }
    }

    /**
     * 预热当前进行中（已开始且未结束）的活动商品库存。 <p>
     * 报名截止时间与活动开始时间一致，开始后商家禁止新增商品， 此时读取的商品集合与库存不会再变化。
     */
    private void preheatStarted() {
        List<SeckillActivityDO> activities = activityMapper.listPreheatActivities(LocalDateTime.now());
        if (activities.isEmpty()) {
            return;
        }

        for (SeckillActivityDO activity : activities) {
            List<SeckillProductDO> products = productMapper.listApprovedByActivityId(activity.getId());
            products.forEach(p -> cacheService.preheatStock(p.getActivityId(), p.getSkuId(), p.getStock()));
        }
    }

    /**
     * 清理已结束/已取消活动的库存 key。
     */
    private void cleanFinished() {
        List<SeckillActivityDO> activities = activityMapper.listFinishedActivities(
                LocalDateTime.now().minusDays(1)
        );
        if (activities.isEmpty()) {
            return;
        }

        for (SeckillActivityDO activity : activities) {
            List<SeckillProductDO> products = productMapper.listByActivityId(activity.getId());
            products.forEach(p -> cacheService.deleteStock(p.getActivityId(), p.getSkuId()));
        }
    }
}
