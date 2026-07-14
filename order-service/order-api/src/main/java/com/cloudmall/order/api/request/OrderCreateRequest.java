package com.cloudmall.order.api.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    private Long userId;
    private Long addressId;
    private List<OrderItemRequest> items;
    private Long couponId;
    private Long seckillId;
    private String remark;

    @Data
    public static class OrderItemRequest {
        private Long goodsId;
        private Long skuId;
        private Integer quantity;
        private String goodsName;
    }
}
