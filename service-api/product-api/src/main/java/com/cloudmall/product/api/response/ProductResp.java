package com.cloudmall.product.api.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductResp {
    private Long id;
    private String name;
    private String spu;
    private String spuCode;
    private String image;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private Integer salesCount;
    private Integer stock;
    private List<String> images;
    private String description;
}