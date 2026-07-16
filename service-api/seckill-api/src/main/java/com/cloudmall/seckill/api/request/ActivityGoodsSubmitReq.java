package com.cloudmall.seckill.api.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActivityGoodsSubmitReq {

    private Long goodsId;
    private BigDecimal seckillPrice;
    private Integer stock;
}