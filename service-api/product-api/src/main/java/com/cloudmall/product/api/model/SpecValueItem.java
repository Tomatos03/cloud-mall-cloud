package com.cloudmall.product.api.model;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格值。
 * <p>
 * 代表规格维度下的一个具体选项（如"红色"、"XL"）。
 * SKU 通过 {@link SkuItem#getSpecValues()} 按 {@code value} 名称引用规格值，
 * 排序由服务端按该值在组内的下标推导，客户端无需传入。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecValueItem {

    @NotBlank(message = "规格值不能为空")
    private String value;

    /** 规格值图片（颜色色块或图标 URL），仅可视化规格（如颜色）需要 */
    private String image;
}
