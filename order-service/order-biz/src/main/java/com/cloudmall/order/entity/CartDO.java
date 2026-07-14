package com.cloudmall.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cm_cart")
public class CartDO {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Long skuId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    private Integer quantity;
    private Boolean selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
