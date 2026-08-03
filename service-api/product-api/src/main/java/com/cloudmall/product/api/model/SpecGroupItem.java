package com.cloudmall.product.api.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格组。
 * <p>
 * 代表商品的一个规格维度（如"颜色"、"尺码"），
 * 包含该维度下所有可选规格值。
 * <p>
 * 规格组与规格值的排序均由服务端按数组下标推导，客户端无需传入 sortOrder。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecGroupItem {

    @NotBlank(message = "规格组名称不能为空")
    private String name;

    @Valid
    @NotEmpty(message = "规格组下至少需要一个规格值")
    private List<SpecValueItem> values;
}
