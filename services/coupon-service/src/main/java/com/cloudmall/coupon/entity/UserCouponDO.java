package com.cloudmall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
