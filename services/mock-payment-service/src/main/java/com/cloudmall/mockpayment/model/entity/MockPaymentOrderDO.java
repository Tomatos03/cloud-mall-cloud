package com.cloudmall.mockpayment.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.payment.api.enums.TradeStateEnum;

/**
 * 模拟支付单实体。
 * <p>
 * 对应 cm_mock_payment_order 表，以微信支付 API v3 交易模型为蓝本，
 * amount_total 使用分存储，与微信支付保持同一金额单位。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_mock_payment_order")
public class MockPaymentOrderDO {

    /** 主键 */
    private Long id;

    /** 商户订单号 */
    private String outTradeNo;

    /** 模拟微信支付订单号 */
    private String transactionId;

    /** 公众号 APPID */
    private String appid;

    /** 商户号 */
    private String mchid;

    /** 商品描述 */
    private String description;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 附加数据 */
    private String attach;

    /** 订单总金额（分） */
    private Integer amountTotal;

    /** 币种 */
    private String currency;

    /** 支付者标识 */
    private String payerOpenid;

    /** 交易类型（JSAPI/NATIVE） */
    private String tradeType;

    /** 交易状态（NOTPAY/SUCCESS/CLOSED 等） */
    private String tradeState;

    /** 交易状态描述 */
    private String tradeStateDesc;

    /** JSAPI 预支付会话标识 */
    private String prepayId;

    /** Native 二维码链接 */
    private String codeUrl;

    /** 支付完成时间 */
    private LocalDateTime successTime;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 是否删除（0=否 1=是） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;

    /**
     * 是否未支付且已过期。
     *
     * @param now 当前时间
     * @return true 表示未支付且已过期
     */
    public boolean isExpiredAt(LocalDateTime now) {
        return TradeStateEnum.NOTPAY.getCode().equals(this.tradeState) && this.expireTime != null
                       && this.expireTime.isBefore(now);
    }
}
