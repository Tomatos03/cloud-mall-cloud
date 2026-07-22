package com.cloudmall.goods.api.response;

import java.math.BigDecimal;
import java.util.List;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GoodsResp {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private Integer salesCount;
    private Integer stock;
    private StatusEnum status;
    private List<String> images;
    private String description;
}