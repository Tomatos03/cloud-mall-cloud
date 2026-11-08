package com.cloudmall.user.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分兑换记录。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_point_exchange_order")
public class PointExchangeOrderDO {

    @TableId
    private Long id;

    private Long userId;

    private Long itemId;

    private Integer pointsCost;

    /** SUCCESS / FAILED */
    private String status;

    private Long userCouponId;

    private String idempotencyKey;

    private LocalDateTime createTime;
}
