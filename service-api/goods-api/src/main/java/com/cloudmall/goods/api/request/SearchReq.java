package com.cloudmall.goods.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchReq {
    private Long categoryId;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 20;
    private String sortBy; // price_asc, price_desc, sales, newest
}