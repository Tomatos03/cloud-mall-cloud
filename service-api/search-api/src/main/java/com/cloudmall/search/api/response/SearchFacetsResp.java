package com.cloudmall.search.api.response;

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
}
