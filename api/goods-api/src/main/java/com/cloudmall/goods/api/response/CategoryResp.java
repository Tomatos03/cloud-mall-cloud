package com.cloudmall.goods.api.response;

import lombok.Data;

import java.util.List;

@Data
public class CategoryResp {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private List<CategoryResp> children;
}
