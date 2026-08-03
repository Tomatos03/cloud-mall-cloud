package com.cloudmall.coupon.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券分类范围实体
 * <p>
 * 对应 cm_coupon_category 表，scope_type=CATEGORY 时命中所选分类及全部后代分类。
 * 联合主键 (coupon_id, category_id)。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_coupon_category")
public class CouponCategoryDO {

    /** 模板 ID */
    private Long couponId;

    /** 分类树根节点 ID */
    private Long categoryId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
