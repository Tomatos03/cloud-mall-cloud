package com.cloudmall.cart.api.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloudmall.cart.api.client.fallback.InternalCartClientFallbackFactory;
import com.cloudmall.cart.api.model.resp.CartSelectedItemResp;

/**
 * 购物车内部 Feign 客户端
 * <p>
 * 内部接口统一收敛于 /internal/cart，仅供服务间调用，不加入网关对外路由。
 *
 * @author : Tomatos
 */
@FeignClient(
        name = "cart-service",
        contextId = "internalCartClient",
        path = "/internal/cart",
        fallbackFactory = InternalCartClientFallbackFactory.class
)
public interface InternalCartClient {

    /**
     * 查询当前用户已选中的购物车项。
     *
     * @param userId 用户 ID
     * @return 已选中购物车项列表
     */
    @GetMapping("/selected-items")
    List<CartSelectedItemResp> listSelectedItems(@RequestParam("userId") Long userId);

    /**
     * 删除用户购物车中的指定 SKU（下单成功后清理，幂等）。
     *
     * @param userId 用户 ID
     * @param skuIds SKU ID 列表
     */
    @DeleteMapping("/items")
    void removeItems(@RequestParam("userId") Long userId, @RequestBody List<Long> skuIds);
}
