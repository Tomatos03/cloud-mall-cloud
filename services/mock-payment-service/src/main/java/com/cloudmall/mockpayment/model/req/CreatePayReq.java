package com.cloudmall.mockpayment.model.req;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信支付 API v3 下单请求。
 * <p>
 * 字段命名对齐微信支付 snake_case 报文；JSAPI 与 Native 共用请求体，
 * 由接口路径决定交易类型，JSAPI 场景额外校验 payer.openid。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePayReq {

    /** 公众号 APPID */
    @NotBlank
    @Size(max = 32)
    private String appid;

    /** 商户号 */
    @NotBlank
    @Size(max = 32)
    private String mchid;

    /** 商品描述 */
    @NotBlank
    @Size(max = 127)
    private String description;

    /** 商户订单号 */
    @NotBlank
    @Size(max = 32)
    private String outTradeNo;

    /** 支付截止时间（RFC3339） */
    @Size(max = 64)
    private String timeExpire;

    /** 附加数据 */
    @Size(max = 127)
    private String attach;

    /** 回调地址 */
    @NotBlank
    @Size(max = 500)
    private String notifyUrl;

    /** 商品标记 */
    @Size(max = 32)
    private String goodsTag;

    /** 是否支持开发票 */
    private Boolean supportFapiao;

    /** 订单金额 */
    @Valid
    @NotNull
    private PayAmountReq amount;

    /** 支付者信息 */
    @Valid
    private PayerReq payer;
}
