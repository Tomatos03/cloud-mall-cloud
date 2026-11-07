package com.cloudmall.product.api.client.fallback;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.product.api.client.BannerClient;
import com.cloudmall.product.api.model.resp.BannerResp;

/**
 * 轮播图客户端兜底工厂
 * <p>
 * BannerClient 仅首页聚合使用，远程调用失败时降级为空分页结果，首页轮播图块显示为空。
 * 由 openfeign 直接实例化（无参构造），无需注册为 Spring bean。
 *
 * @author : Tomatos
 */
@Slf4j
public class BannerClientFallbackFactory implements FallbackFactory<BannerClient> {

    /**
     * 构建兜底客户端。
     *
     * @param cause 失败原因
     * @return 兜底客户端
     */
    @Override
    public BannerClient create(Throwable cause) {
        return new BannerClient() {

            /**
             * 分页查询轮播图（降级为空分页）。
             *
             * @param current 当前页码
             * @param size 每页条数
             * @return 空分页结果
             */
            @Override
            public Page<BannerResp> page(long current, long size) {
                log.warn("轮播图远程调用失败，首页降级为空列表", cause);
                return new Page<>();
            }
        };
    }
}
