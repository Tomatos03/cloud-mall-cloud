package com.cloudmall.mockpayment.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信支付订单金额响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AmountResp {

    /** 订单总金额（分） */
    private Integer total;

    /** 用户实际支付金额（分） */
    private Integer payerTotal;

    /** 币种 */
    private String currency;

    /** 支付币种 */
    private String payerCurrency;
}
