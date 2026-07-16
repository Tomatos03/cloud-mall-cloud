package com.cloudmall.order.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemResp {

    private Long id;
    private Long goodsId;
    private Long skuId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
