package com.cloudmall.user.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分流水。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_point_log")
public class UserPointLogDO {

    @TableId
    private Long id;

    private Long userId;

    /** 积分变动值，获得为正、消耗为负 */
    private Integer changeAmount;

    /** 变动后的积分余额 */
    private Integer balanceAfter;

    /** 流水类型：ORDER / SIGN_IN / COMMENT / EXCHANGE / ADMIN */
    private String type;

    private String description;

    /** 幂等键（订单支付等事件防重） */
    private String idempotencyKey;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
