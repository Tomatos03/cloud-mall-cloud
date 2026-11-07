package com.cloudmall.product.api.model.req;

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

import com.cloudmall.product.api.model.bo.ImageItem;
import com.cloudmall.product.api.model.bo.SkuItem;
import com.cloudmall.product.api.model.bo.SpecGroupItem;

/**
 * 商品更新请求。
 * <p>
 * 结构与创建请求一致：SPU 基本信息 + 规格分组 + SKU + 图片。
 * SPU ID 通过路径参数传入。
 * <p>
 * 更新为差异合并语义：规格/ SKU 按规格组合指纹匹配，已存在的 SKU 保留
 * 主键、SKU 编码与库存，仅刷新价格等销售字段；新增规格组合则插入新 SKU。
 *
 * @see ProductCreateReq 创建请求（字段一致）
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateReq {

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
