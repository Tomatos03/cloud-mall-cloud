package com.cloudmall.seckill.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.seckill.api.client.SeckillClient;
import com.cloudmall.seckill.api.model.resp.ActivityDetailResp;

/**
 * 秒杀客户端兜底工厂
 * <p>
 * SeckillClient 仅首页聚合使用，远程调用失败时降级为 null，首页秒杀区隐藏。
 * 由 openfeign 直接实例化（无参构造），无需注册为 Spring bean。
 *
 * @author : Tomatos
 */
@Slf4j
public class SeckillClientFallbackFactory implements FallbackFactory<SeckillClient> {

    /**
     * 构建兜底客户端。
     *
     * @param cause 失败原因
     * @return 兜底客户端
     */
    @Override
    public SeckillClient create(Throwable cause) {
        return new SeckillClient() {

            /**
             * 查询活动详情（降级为 null）。
             *
             * @param id 活动 ID
             * @return null
             */
            @Override
            public ActivityDetailResp getActivity(Long id) {
                log.warn("秒杀活动详情远程调用失败，首页降级为 null", cause);
                return null;
            }

            /**
             * 查询当前整点场次的进行中活动（降级为 null）。
             *
             * @return null
             */
            @Override
            public ActivityDetailResp getCurrentHourActivity() {
                log.warn("秒杀当前整点活动远程调用失败，首页降级为 null", cause);
                return null;
            }
        };
    }
}
