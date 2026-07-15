package com.cloudmall.goods.api.response;

import lombok.Data;

import java.util.List;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private List<CategoryResponse> children;
}
