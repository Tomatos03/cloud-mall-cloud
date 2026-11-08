package com.cloudmall.product.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.model.resp.SpecGroupResp;
import com.cloudmall.product.convert.SpecGroupConverter;
import com.cloudmall.product.mapper.ProductSkuSpecMapper;
import com.cloudmall.product.mapper.ProductSpecGroupMapper;
import com.cloudmall.product.mapper.ProductSpecValueMapper;
import com.cloudmall.product.model.entity.ProductSkuSpecDO;
import com.cloudmall.product.model.entity.ProductSpecGroupDO;
import com.cloudmall.product.model.entity.ProductSpecValueDO;
import com.cloudmall.product.service.IProductSpecService;

@Service
@RequiredArgsConstructor
public class ProductSpecService implements IProductSpecService {

    private final ProductSpecGroupMapper productSpecGroupMapper;
    private final ProductSpecValueMapper productSpecValueMapper;
    private final ProductSkuSpecMapper productSkuSpecMapper;
    private final SpecGroupConverter specGroupConverter;

    /**
     * 获取相关数据。
     *
     * @param productId 商品标识
     * @return 数据列表
     */
    @Override
    public List<SpecGroupResp> getSpecGroups(Long productId) {
        List<ProductSpecGroupDO> groups = productSpecGroupMapper.selectByProductId(productId);
        List<SpecGroupResp> responses = new ArrayList<>(groups.size());
        for (ProductSpecGroupDO group : groups) {
            List<ProductSpecValueDO> values = productSpecValueMapper.selectByGroupId(group.getId());
            responses.add(specGroupConverter.toResp(group, values));
        }
        return responses;
    }

    /**
     * 获取相关数据。
     *
     * @param skuIds SKU 标识列表
     * @return 数据列表
     */
    @Override
    public Map<Long, List<Long>> getSkuSpecMap(List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Map.of();
        }
        List<ProductSkuSpecDO> specs = productSkuSpecMapper.selectBySkuIds(skuIds);
        return specs.stream()
                    .collect(
                            Collectors.groupingBy(
                                    ProductSkuSpecDO::getSkuId,
                                    Collectors.mapping(ProductSkuSpecDO::getSpecValueId, Collectors.toList())
                            )
                    );
    }

    /**
     * 获取相关数据。
     *
     * @param productId 商品标识
     * @param specValueIds 规格值标识列表
     * @return 数据列表
     */
    @Override
    public Map<Long, List<Long>> getSpecValueSkuMap(Long productId, List<Long> specValueIds) {
        if (CollUtil.isEmpty(specValueIds)) {
            return Map.of();
        }
        List<ProductSkuSpecDO> specs = productSkuSpecMapper.selectBySpecValueIdsInProduct(
                specValueIds,
                productId
        );
        return specs
                .stream()
                .collect(
                        Collectors.groupingBy(
                                ProductSkuSpecDO::getSkuId,
                                Collectors.mapping(ProductSkuSpecDO::getSpecValueId, Collectors.toList())
                        )
                )
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() == specValueIds.size())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
