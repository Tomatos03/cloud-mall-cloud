package com.cloudmall.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cm_goods_sku")
public class GoodsSkuDO {
    private Long id;
    private Long goodsId;
    private String specIds;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
