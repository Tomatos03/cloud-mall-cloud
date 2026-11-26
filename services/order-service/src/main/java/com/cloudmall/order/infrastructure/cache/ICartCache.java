package com.cloudmall.order.infrastructure.cache;

import java.util.List;
import java.util.function.Supplier;

import com.cloudmall.order.model.entity.CartDO;

/**
 * 购物车缓存契约。
 * <p>
 * 缓存只服务展示读，未命中时通过 loader 回源数据库；不参与判重、容量校验或下单取数。
 *
 * @author : Tomatos
 */
public interface ICartCache {

    /**
     * 读取购物车记录快照，未命中时执行 loader 回源并回填缓存。
     *
     * @param userId 用户 ID
     * @param loader 回源数据库的操作
     * @return 购物车记录列表
     */
    List<CartDO> getOrLoad(Long userId, Supplier<List<CartDO>> loader);

    /**
     * 失效指定用户的购物车缓存。
     *
     * @param userId 用户 ID
     */
    void evict(Long userId);
}
