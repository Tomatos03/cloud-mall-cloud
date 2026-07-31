package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.model.SkuItem;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.product.entity.ProductSkuDO;

/**
 * SKU 对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface SkuConverter {

    /**
     * 将 SKU 请求模型转换为实体。
     * <p>
     * specHash 由 Service 层在解析出规格值 ID 后赋值。
     *
     * @param item      SKU 请求模型
     * @param productId 所属商品 ID
     * @param skuCode   SKU 编码
     * @return SKU 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "skuCode", source = "skuCode")
    @Mapping(target = "specHash", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ProductSkuDO toDO(SkuItem item, Long productId, String skuCode);

    /**
     * 将 SKU 实体转换为 SKU 响应。
     *
     * @param sku SKU 实体
     * @return SKU 响应
     */
    @Mapping(target = "skuId", source = "id")
    SkuResp toResp(ProductSkuDO sku);
}
