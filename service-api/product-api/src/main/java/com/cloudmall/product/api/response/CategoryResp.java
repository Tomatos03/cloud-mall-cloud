package com.cloudmall.product.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类响应对象
 * <p>
 * 作为业务层数据载体，不包含树形结构。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResp {

    /** 分类 ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类 ID（0 表示根节点） */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;
}
