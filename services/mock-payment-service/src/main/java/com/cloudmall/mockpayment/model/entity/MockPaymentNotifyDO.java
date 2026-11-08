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

/**
 * 模拟支付回调通知记录。
 * <p>
 * 每条记录对应一次待投递或已投递的微信支付风格回调，
 * request_body 保存加密后的原始报文，重试时复用同一份报文并重新签名。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_mock_payment_notify")
public class MockPaymentNotifyDO {

    /** 主键 */
    private Long id;

    /** 商户订单号 */
    private String outTradeNo;

    /** 回调事件类型 */
    private String eventType;

    /** 回调地址 */
    private String notifyUrl;

    /** 加密后的回调报文 */
    private String requestBody;

    /** 最近一次签名值 */
    private String signature;

    /** 最近一次应答报文 */
    private String responseBody;

    /** 投递状态（PENDING/SUCCESS/FAILED） */
    private String status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 最近一次失败原因 */
    private String lastError;

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
}
