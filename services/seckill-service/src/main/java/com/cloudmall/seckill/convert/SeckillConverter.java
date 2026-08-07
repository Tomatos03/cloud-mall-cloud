package com.cloudmall.seckill.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.seckill.api.response.ActivityDetailResp;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.api.response.SeckillOrderResp;
import com.cloudmall.seckill.api.response.SeckillProductResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillOrderDO;
import com.cloudmall.seckill.entity.SeckillProductDO;

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
}
