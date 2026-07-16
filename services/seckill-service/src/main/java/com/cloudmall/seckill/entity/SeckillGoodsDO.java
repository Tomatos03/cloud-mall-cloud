package com.cloudmall.seckill.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("cm_seckill_goods")
public class SeckillGoodsDO {
    private Long id;
    private Long activityId;
    private Long goodsId;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer soldCount;
    private String auditStatus;
}