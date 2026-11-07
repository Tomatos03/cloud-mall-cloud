package com.cloudmall.product.api.model.req;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按规格值匹配 SKU 请求。
 * <p>
 * 用于商品详情页规格选择联动：传入商品 ID 与已选规格值 ID 列表，
 * 当所有规格组均有选值时唯一命中一个 SKU。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuMatchReq {

    /** 商品 ID */
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    /** 已选规格值 ID 列表 */
    @NotEmpty(message = "规格值列表不能为空")
    private List<Long> specValueIds;
}
