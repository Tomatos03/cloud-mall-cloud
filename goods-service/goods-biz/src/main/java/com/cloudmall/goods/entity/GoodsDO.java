package com.cloudmall.goods.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cm_goods")
public class GoodsDO {
    private Long id;
    private String name;
    private String image;
    private String images;
    private String description;
    private Long categoryId;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private Integer salesCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
