package com.cloudmall.product.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollectionUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.model.SkuItem;
import com.cloudmall.product.api.response.ProductSku;
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
     * 将 SKU 请求模型转换为实体。 <p>
     * specHash 由 Service 层在解析出规格值 ID 后赋值。
     *
     * @param item SKU 请求模型
     * @param productId 所属商品 ID
     * @param skuCode SKU 编码
     * @return SKU 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "specHash", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "skuCode", source = "skuCode")
    @Mapping(target = "productId", source = "productId")
    ProductSkuDO toDO(SkuItem item, Long productId, String skuCode);

    /**
     * 将 SKU 实体转换为 SKU 响应。
     *
     * @param sku SKU 实体
     * @return SKU 响应
     */
    @Mapping(target = "skuId", source = "id")
    SkuResp toResp(ProductSkuDO sku);

    /**
     * 将 SKU 实体转换为商品详情 SKU 结构。
     *
     * @param sku SKU 实体
     * @param specValueIds 该 SKU 关联的规格值 ID 列表
     * @return 商品详情 SKU 结构
     */
    @Mapping(target = "specValueIds", source = "specValueIds")
    ProductSku toResp(ProductSkuDO sku, List<Long> specValueIds);

    /**
     * 批量将 SKU 实体转换为商品详情 SKU 结构。
     *
     * @param skus SKU 实体列表
     * @param skuSpecMap SKU ID -> 规格值 ID 列表映射
     * @return 商品详情 SKU 结构列表
     */
    default List<ProductSku> toRespList(List<ProductSkuDO> skus, Map<Long, List<Long>> skuSpecMap) {
        if (CollectionUtil.isEmpty(skus)) {
            return Collections.emptyList();
        }
        List<ProductSku> responses = new ArrayList<>(skus.size());
        for (ProductSkuDO sku : skus) {
            responses.add(toResp(sku, skuSpecMap.get(sku.getId())));
        }
        return responses;
    }
}
