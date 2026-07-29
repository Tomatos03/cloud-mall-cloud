package com.cloudmall.goods.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryCreateReq {

    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String icon;
}
