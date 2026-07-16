package com.cloudmall.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("cm_order_item")
public class OrderItemDO {
    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long skuId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
