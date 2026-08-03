package com.cloudmall.seckill.api.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityProductSubmitReq {

    private Long productId;
    private BigDecimal seckillPrice;
    private Integer stock;
}
