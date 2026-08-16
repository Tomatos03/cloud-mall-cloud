package com.cloudmall.product.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateReq {

    private String name;
    private Long parentId;
    private Integer sort;
    private String icon;
}
