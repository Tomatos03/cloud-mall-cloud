package com.cloudmall.goods.entity;

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