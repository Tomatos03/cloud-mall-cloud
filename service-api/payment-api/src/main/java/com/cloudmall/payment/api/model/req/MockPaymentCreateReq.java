package com.cloudmall.payment.api.model.req;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟支付下单请求。
 * <p>
 * 业务服务通过内部接口调用模拟微信支付服务，金额统一使用分，避免浮点误差。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockPaymentCreateReq {

    /** 商户订单号（业务系统订单号） */
    @NotBlank
    @Size(max = 32)
    private String outTradeNo;

    /** 商品描述 */
    @NotBlank
    @Size(max = 127)
    private String description;

    /** 订单总金额（分） */
    @NotNull
    @Positive
    private Integer amountTotal;

    /** 支付结果回调地址 */
    @NotBlank
    @Size(max = 500)
    private String notifyUrl;

    /** 附加数据，查询与回调原样返回 */
    @Size(max = 127)
    private String attach;

    /** 支付者标识（JSAPI 场景使用，可选） */
    @Size(max = 128)
    private String payerOpenid;

    /** 交易类型：JSAPI / NATIVE */
    private String tradeType;

    /** 支付截止时间，为空时默认下单后 15 分钟 */
    private LocalDateTime expireTime;
}
