package com.cloudmall.seckill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

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
