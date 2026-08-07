package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.seckill.api.response.ActivityDetailResp;

/**
 * 秒杀对外 Feign 客户端
 * <p>
 * 仅暴露对外 /seckill 端点；内部端点见 InternalSeckillClient。
 *
 * @author : Tomatos
 */
@FeignClient(name = "seckill-service", contextId = "seckillClient")
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

}
