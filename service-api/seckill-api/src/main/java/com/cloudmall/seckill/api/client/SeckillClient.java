package com.cloudmall.seckill.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloudmall.seckill.api.response.ActivityDetailResp;

/**
 * 秒杀服务 Feign 客户端
 * <p>
 * 两类端点前缀不同（对外 /seckill、内部 /internal/seckill），故不设 path 公共前缀。
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

    /**
     * 校验活动下的商品是否已审核通过。
     *
     * @param activityId 活动 ID
     * @param productId 秒杀商品 ID
     * @return 已审核通过返回 true
     */
    @GetMapping("/internal/seckill/activities/{activityId}/products/{productId}/verification")
    Boolean verifyProduct(
            @PathVariable("activityId")
            Long activityId,
            @PathVariable("productId")
            Long productId
    );
}
