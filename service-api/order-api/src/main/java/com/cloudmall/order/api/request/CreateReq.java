package com.cloudmall.order.api.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateReq {

    private Long userId;
    private Long addressId;
    private List<OrderItemRequest> items;
    private Long couponId;
    private Long seckillId;
    private String remark;

@Builder
@NoArgsConstructor
@AllArgsConstructor
    @Data
    public static class OrderItemRequest {
        private Long goodsId;
        private Long skuId;
        private Integer quantity;
        private String goodsName;
        private BigDecimal price;
    }
}