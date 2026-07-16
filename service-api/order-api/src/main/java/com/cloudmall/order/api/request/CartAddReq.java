package com.cloudmall.order.api.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartAddReq {

    private Long userId;
    private Long goodsId;
    private Long skuId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    private Integer quantity;
}
