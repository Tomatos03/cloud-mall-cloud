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

import com.cloudmall.order.api.enums.CheckoutTypeEnum;
import com.cloudmall.order.enums.ParentOrderStatusEnum;

/**
 * 父订单实体
 * <p>
 * 负责支付聚合和用户视角的完整订单，跨店结算时聚合多个店铺子订单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_order")
public class OrderDO {

    /** 父订单 ID */
    @TableId
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 商品原价总额 */
    private BigDecimal totalOriginalAmount;

    /** 店铺券优惠总额 */
    private BigDecimal totalShopDiscountAmount;

    /** 平台券优惠总额 */
    private BigDecimal totalPlatformDiscountAmount;

    /** 商品应付金额（原价减全部优惠） */
    private BigDecimal goodsPayableAmount;

    /** 运费金额 */
    private BigDecimal freightAmount;

    /** 最终应付金额（商品应付加运费） */
    private BigDecimal payableAmount;

    /** 订单状态 */
    private ParentOrderStatusEnum status;

    /** 订单类型（普通/秒杀） */
    private CheckoutTypeEnum orderType;

    /** 收货地址快照（JSON） */
    private String addressSnapshot;

    /** 订单备注 */
    private String remark;

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
