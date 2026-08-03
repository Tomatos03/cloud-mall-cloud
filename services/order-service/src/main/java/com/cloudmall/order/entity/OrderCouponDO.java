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

import com.cloudmall.coupon.enums.CouponOwnerTypeEnum;
import com.cloudmall.coupon.enums.CouponTypeEnum;

/**
 * 订单优惠关联实体
 * <p>
 * 记录本次订单实际使用的用户券及其计算快照，不重新读取当前模板计算历史金额。
 * 平台券关联父订单，店铺券关联对应店铺子订单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_order_coupon")
public class OrderCouponDO {

    /** 关联 ID */
    @TableId
    private Long id;

    /** 父订单 ID */
    private Long parentOrderId;

    /** 子订单 ID（平台券为 null） */
    private Long subOrderId;

    /** 用户券 ID */
    private Long userCouponId;

    /** 优惠券模板 ID */
    private Long couponId;

    /** 券归属 */
    private CouponOwnerTypeEnum ownerType;

    /** 店铺 ID */
    private Long shopId;

    /** 券类型快照 */
    private CouponTypeEnum typeSnapshot;

    /** 门槛快照 */
    private BigDecimal thresholdSnapshot;

    /** 金额优惠快照 */
    private BigDecimal discountAmountSnapshot;

    /** 比例优惠快照 */
    private BigDecimal discountRateSnapshot;

    /** 适用金额 */
    private BigDecimal applicableAmount;

    /** 实际优惠金额 */
    private BigDecimal discountAmount;

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
