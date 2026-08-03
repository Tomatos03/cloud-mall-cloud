package com.cloudmall.product.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按订单释放库存请求
 * <p>
 * 内部接口，由订单服务在取消/超时/补偿场景调用：
 * 释放该订单全部预占（RESERVE）流水对应的库存，幂等。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReleaseByOrderReq {

    /** 订单号（幂等键） */
    @NotBlank
    private String orderNo;
}
