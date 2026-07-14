package com.cloudmall.goods.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsResponse {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private Integer salesCount;
    private Integer stock;
    private String status;
    private List<String> images;
    private String description;
}
