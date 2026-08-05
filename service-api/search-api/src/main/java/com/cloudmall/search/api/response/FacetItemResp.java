package com.cloudmall.search.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聚合筛选项响应
 * <p>
 * 表示分类/品牌聚合结果中的单个桶：ID + 名称 + 命中数。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacetItemResp {

    /** 聚合项 ID（分类 ID / 品牌 ID） */
    private Long id;

    /** 聚合项名称 */
    private String name;

    /** 命中数量 */
    private Long count;
}
