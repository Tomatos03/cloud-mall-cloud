package com.cloudmall.product.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类树节点
 * <p>
 * 包含分类基本信息及其子节点列表，用于构建前端分类树。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryNodeResp {

    /** 分类信息 */
    @JsonUnwrapped
    private CategoryResp category;

    /** 子节点列表（叶子节点为空列表） */
    private List<CategoryNodeResp> children;
}
