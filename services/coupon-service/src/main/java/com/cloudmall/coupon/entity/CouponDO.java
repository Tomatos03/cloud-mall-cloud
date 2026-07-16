package com.cloudmall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
