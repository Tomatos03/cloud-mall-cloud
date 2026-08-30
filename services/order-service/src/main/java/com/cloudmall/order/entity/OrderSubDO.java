package com.cloudmall.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.order.enums.SubOrderStatusEnum;

/**
 * 店铺子订单实体
 * <p>
 * 每个父订单每个店铺最多一条子订单，负责履约。
 * 父订单负责支付聚合，子订单负责发货、售后等履约状态。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_order_sub")
public class OrderSubDO {

    /** 子订单 ID */
    @TableId
    private Long id;

    /** 子订单编号 */
    private String subOrderNo;

    /** 父订单 ID */
    private Long parentOrderId;

    /** 店铺 ID */
    private Long shopId;

    /** 商品原价金额 */
    private BigDecimal originalAmount;

    /** 店铺券优惠金额 */
    private BigDecimal shopDiscountAmount;

    /** 平台券优惠金额 */
    private BigDecimal platformDiscountAmount;

    /** 商品应付金额 */
    private BigDecimal payableAmount;

    /** 商家结算金额 */
    private BigDecimal merchantSettlementAmount;

    /** 订单备注（店铺维度，跨店铺聚合订单按店铺单独设置） */
    private String remark;

    /** 子订单状态 */
    private SubOrderStatusEnum status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
