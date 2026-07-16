package com.cloudmall.coupon.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("cm_user_coupon")
public class UserCouponDO {
    private Long id;
    private Long userId;
    private Long couponId;
    private String status; // UNUSED/USED/EXPIRED
    private LocalDateTime claimedTime;
    private LocalDateTime usedTime;
    private Long orderId;
}