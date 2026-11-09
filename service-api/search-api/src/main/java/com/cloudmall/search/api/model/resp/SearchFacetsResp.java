package com.cloudmall.search.api.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索聚合结果响应
 * <p>
 * 侧边筛选栏数据：分类聚合 + 品牌聚合。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFacetsResp {

    /** 分类聚合 */
    private List<FacetItemResp> categories;

    /** 品牌聚合 */
    private List<FacetItemResp> brands;

    /** 分类去重数量（不受聚合条数上限影响） */
    private Long categoryCount;

    /** 品牌去重数量（不受聚合条数上限影响） */
    private Long brandCount;
}
