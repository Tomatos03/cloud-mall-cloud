package com.cloudmall.coupon.entity;

import java.math.BigDecimal;
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
@TableName("cm_coupon")
public class CouponDO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal threshold;
    private BigDecimal discount;
    private Integer totalCount;
    private Integer claimedCount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}