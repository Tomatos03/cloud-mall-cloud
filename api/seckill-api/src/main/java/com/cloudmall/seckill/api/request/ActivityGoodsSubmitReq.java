package com.cloudmall.seckill.api.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityGoodsSubmitReq {

    private Long goodsId;
    private BigDecimal seckillPrice;
    private Integer stock;
}
