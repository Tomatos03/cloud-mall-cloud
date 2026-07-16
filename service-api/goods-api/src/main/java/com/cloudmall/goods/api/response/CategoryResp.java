package com.cloudmall.goods.api.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryResp {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private List<CategoryResp> children;
}