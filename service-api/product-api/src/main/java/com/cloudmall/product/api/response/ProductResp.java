package com.cloudmall.product.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResp {
    private Long id;
    private String name;
    private String spuCode;
    private String image;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private String description;

    /** 所属店铺 ID */
    private Long shopId;
}
