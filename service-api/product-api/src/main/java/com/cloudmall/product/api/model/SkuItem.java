package com.cloudmall.product.api.model;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SKU 项。
 * <p>
 * 代表商品的一个可售卖 SKU，通过 {@code specValues} 引用该 SKU 对应的规格值组合：
 * 第 g 个元素是 {@code specGroups[g]} 里选中的规格值名称，
 * 因此列表长度必须等于规格组数量，且元素顺序与规格组顺序严格对应。
 * <p>
 * 服务端按组隔离查找 —— 第 g 位的值只在第 g 个规格组的候选里匹配，
 * 跨组引用（如把尺码值写在颜色位上）会被拒绝。
 * <p>
 * {@code skuCode} 由服务端自动生成，客户端无需传入。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuItem {

    /** 该 SKU 选中的规格值名称，按 {@code specGroups} 的顺序逐组给出，每组恰好一个 */
    @NotEmpty(message = "SKU 必须指定规格值")
    private List<String> specValues;

    @NotNull(message = "SKU 价格不能为空")
    @DecimalMin(value = "0.01", message = "SKU 价格必须大于零")
    private BigDecimal price;

    private BigDecimal marketPrice;

    @NotNull(message = "SKU 库存不能为空")
    private Integer stock;

    private BigDecimal weight;

    /** SKU 专属图URL */
    private String image;
}
