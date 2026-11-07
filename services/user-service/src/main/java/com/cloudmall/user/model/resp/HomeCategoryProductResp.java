package com.cloudmall.user.model.resp;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.product.api.model.resp.CategoryResp;
import com.cloudmall.product.api.model.resp.ProductDocResp;

/**
 * 首页分类展示商品分组
 * <p>
 * 一级分类为一个展示分组，二级分类为切换标签，每个标签下展示若干商品。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeCategoryProductResp {

    /** 一级分类 */
    private CategoryResp category;

    /** 二级分类标签 */
    private List<CategoryResp> tabs;

    /** 标签分类 ID → 商品列表 */
    private Map<Long, List<ProductDocResp>> productMap;
}
