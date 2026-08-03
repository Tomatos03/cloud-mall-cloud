package com.cloudmall.product.api.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.product.api.model.ImageItem;
import com.cloudmall.product.api.model.SkuItem;
import com.cloudmall.product.api.model.SpecGroupItem;

/**
 * 商品创建请求。
 * <p>
 * 聚合 SPU 基本信息、规格分组、SKU 及图片，各嵌套结构定义见 {@code model} 包。
 * <p>
 * 每个 SKU 通过 {@link SkuItem#getSpecValues()} 按 {@code specGroups} 的顺序逐组给出选中的规格值名称，
 * 服务端据此建立 SKU 与规格值的关联。
 * <p>
 * {@code spuCode}、{@code skuCode}、图片主图标记与各处排序均由服务端推导，客户端无需传入。
 *
 * @see SpecGroupItem 规格组
 * @see com.cloudmall.product.api.model.SpecValueItem 规格值
 * @see SkuItem SKU 项
 * @see ImageItem 图片
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateReq {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    @NotBlank(message = "商品描述不能为空")
    private String description;

    @NotNull(message = "分类 ID 不能为空")
    private Long categoryId;

    @NotNull(message = "品牌 ID 不能为空")
    private Long brandId;

    @Valid
    @NotEmpty(message = "规格分组不能为空")
    @Size(max = 3, message = "规格分组最多 3 组")
    private List<SpecGroupItem> specGroups;

    @Valid
    @NotEmpty(message = "SKU 列表不能为空")
    private List<SkuItem> skus;

    /** 商品图片，首张为主图 */
    @Valid
    @NotEmpty(message = "图片列表不能为空")
    private List<ImageItem> images;
}
