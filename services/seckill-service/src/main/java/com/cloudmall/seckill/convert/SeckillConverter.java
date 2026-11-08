package com.cloudmall.seckill.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.seckill.api.enums.SeckillSessionStatusEnum;
import com.cloudmall.seckill.api.model.resp.ActivityDetailResp;
import com.cloudmall.seckill.api.model.resp.ActivityResp;
import com.cloudmall.seckill.api.model.resp.SeckillOrderResp;
import com.cloudmall.seckill.api.model.resp.SeckillProductResp;
import com.cloudmall.seckill.api.model.resp.SeckillSessionResp;
import com.cloudmall.seckill.model.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.entity.SeckillOrderDO;
import com.cloudmall.seckill.model.entity.SeckillProductDO;

/**
 * 秒杀模块转换器
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface SeckillConverter {

    /**
     * 活动实体转为列表视图响应。
     *
     * @param activity 活动实体
     * @return 活动响应
     */
    ActivityResp toActivityResp(SeckillActivityDO activity);

    /**
     * 活动实体列表转为列表视图响应。
     *
     * @param activities 活动实体列表
     * @return 活动响应列表
     */
    List<ActivityResp> toActivityRespList(List<SeckillActivityDO> activities);

    /**
     * 活动实体转为详情视图响应（不含商品）。
     *
     * @param activity 活动实体
     * @return 活动详情响应
     */
    ActivityDetailResp toActivityDetailResp(SeckillActivityDO activity);

    /**
     * 秒杀商品实体转为响应。
     *
     * @param product 秒杀商品实体
     * @return 秒杀商品响应
     */
    SeckillProductResp toSeckillProductResp(SeckillProductDO product);

    /**
     * 秒杀商品实体列表转为响应列表。
     *
     * @param products 秒杀商品实体列表
     * @return 秒杀商品响应列表
     */
    List<SeckillProductResp> toSeckillProductRespList(List<SeckillProductDO> products);

    /**
     * 秒杀单实体转为订单响应。
     *
     * @param order 秒杀单实体
     * @return 秒杀订单响应
     */
    SeckillOrderResp toSeckillOrderResp(SeckillOrderDO order);

    /**
     * 秒杀单实体列表转为订单响应列表。
     *
     * @param orders 秒杀单实体列表
     * @return 秒杀订单响应列表
     */
    List<SeckillOrderResp> toSeckillOrderRespList(List<SeckillOrderDO> orders);

    /**
     * 组装单个整点场次响应：有活动时取活动信息，无活动时使用当天该整点窗口。
     *
     * @param hour 场次整点
     * @param activity 对应活动，没有时为 null
     * @param today 当天日期
     * @param now 当前时间
     * @return 场次响应
     */
    default SeckillSessionResp toSessionResp(
        int hour,
        SeckillActivityDO activity,
        LocalDate today,
        LocalDateTime now
    ) {
        LocalDateTime slotStart = today.atTime(hour, 0);
        LocalDateTime slotEnd = slotStart.plusHours(1);
        String time = String.format("%02d:00", hour);

        if (activity == null) {
            return SeckillSessionResp.builder()
                                     .hour(hour)
                                     .time(time)
                                     .hasActivity(false)
                                     .startTime(slotStart)
                                     .endTime(slotEnd)
                                     .status(SeckillSessionStatusEnum.NOT_OPENED.getCode())
                                     .build();
        }

        return SeckillSessionResp.builder()
                                 .hour(hour)
                                 .time(time)
                                 .hasActivity(true)
                                 .activityId(activity.getId())
                                 .activityName(activity.getName())
                                 .startTime(activity.getStartTime())
                                 .endTime(activity.getEndTime())
                                 .status(resolveSessionStatus(activity, now).getCode())
                                 .build();
    }

    /**
     * 按活动时间窗口推导场次展示状态。
     *
     * @param activity 活动实体
     * @param now 当前时间
     * @return 场次状态
     */
    default SeckillSessionStatusEnum resolveSessionStatus(SeckillActivityDO activity, LocalDateTime now) {
        if (now.isBefore(activity.getStartTime())) {
            return SeckillSessionStatusEnum.UPCOMING;
        }
        if (now.isBefore(activity.getEndTime())) {
            return SeckillSessionStatusEnum.ONGOING;
        }
        return SeckillSessionStatusEnum.ENDED;
    }
}
