package com.cloudmall.seckill.job;

import java.time.LocalDateTime;
import java.util.List;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.seckill.cache.SeckillCacheService;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;

/**
 * 秒杀缓存预热 JobHandler
 * <p>
 * 提前 30 分钟为即将开始及进行中的活动预热库存 key（SET NX 不覆盖运行时值），
 * 并清理已结束/已取消活动的库存 key。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillCachePreheatJobHandler {

    /** 预热提前量（分钟） */
    private static final long PREHEAT_AHEAD_MINUTES = 30;

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
            preheatUpcoming();
            cleanFinished();
        } catch (Exception e) {
            log.error("秒杀缓存预热失败", e);
        }
    }

    /**
     * 预热报名中/进行中且临近开始的活动商品库存。
     */
    private void preheatUpcoming() {
        List<SeckillActivityDO> activities = activityMapper.listPreheatActivities(
                LocalDateTime.now().plusMinutes(PREHEAT_AHEAD_MINUTES)
        );
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
