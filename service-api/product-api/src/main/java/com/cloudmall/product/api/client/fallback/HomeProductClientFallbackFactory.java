package com.cloudmall.product.api.client.fallback;

import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.product.api.client.HomeProductClient;
import com.cloudmall.product.api.response.ProductDocResp;

/**
 * 首页商品客户端兜底工厂
 * <p>
 * 仅首页聚合使用，远程调用失败时降级为空列表，首页对应分类商品分组为空。
 * 由 openfeign 直接实例化（无参构造），无需注册为 Spring bean。
 *
 * @author : Tomatos
 */
@Slf4j
public class HomeProductClientFallbackFactory implements FallbackFactory<HomeProductClient> {

    /**
     * 构建兜底客户端。
     *
     * @param cause 失败原因
     * @return 兜底客户端
     */
    @Override
    public HomeProductClient create(Throwable cause) {
        return new HomeProductClient() {

            /**
             * 按分类 ID 集合查询已上架商品（降级为空列表）。
             *
             * @param categoryIds 分类 ID 集合
             * @param limit 查询条数上限
             * @return 空列表
             */
            @Override
            public List<ProductDocResp> listByCategory(Set<Long> categoryIds, Integer limit) {
                log.warn("首页分类商品远程调用失败，降级为空列表", cause);
                return List.of();
            }
        };
    }
}
