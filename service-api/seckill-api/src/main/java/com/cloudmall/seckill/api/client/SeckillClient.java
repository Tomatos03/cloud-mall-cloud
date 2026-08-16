package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.seckill.api.client.fallback.SeckillClientFallbackFactory;
import com.cloudmall.seckill.api.response.ActivityDetailResp;

/**
 * 秒杀对外 Feign 客户端
 * <p>
 * 仅暴露对外 /seckill 端点；内部端点见 InternalSeckillClient。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "seckill-service",
        contextId = "seckillClient",
        fallbackFactory = SeckillClientFallbackFactory.class
)
public interface SeckillClient {

    /**
     * 查询活动详情（含已审核商品）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    @GetMapping("/seckill/activities/{id}")
    ActivityDetailResp getActivity(
            @PathVariable("id")
            Long id
    );

    /**
     * 查询当前整点场次的进行中活动（含已审核商品）。
     *
     * @return 活动详情，当前无进行中场次时返回 null
     */
    @GetMapping("/seckill/activities/current-hour")
    ActivityDetailResp getCurrentHourActivity();

}
