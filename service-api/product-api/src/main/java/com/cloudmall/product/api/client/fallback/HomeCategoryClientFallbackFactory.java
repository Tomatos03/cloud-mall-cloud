package com.cloudmall.product.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.product.api.client.HomeCategoryClient;
import com.cloudmall.product.api.model.resp.CategoryResp;

/**
 * 首页分类客户端兜底工厂
 * <p>
 * 仅首页聚合使用，远程调用失败时降级为空列表，首页分类侧栏与分类商品区为空。
 * 由 openfeign 直接实例化（无参构造），无需注册为 Spring bean。
 *
 * @author : Tomatos
 */
@Slf4j
public class HomeCategoryClientFallbackFactory implements FallbackFactory<HomeCategoryClient> {

    /**
     * 构建兜底客户端。
     *
     * @param cause 失败原因
     * @return 兜底客户端
     */
    @Override
    public HomeCategoryClient create(Throwable cause) {
        return new HomeCategoryClient() {

            /**
             * 查询分类平铺列表（降级为空列表）。
             *
             * @return 空列表
             */
            @Override
            public List<CategoryResp> list() {
                log.warn("首页分类远程调用失败，降级为空列表", cause);
                return List.of();
            }
        };
    }
}
