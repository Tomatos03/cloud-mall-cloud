package com.cloudmall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.response.SpecGroupResp;
import com.cloudmall.product.convert.SpecGroupConverter;
import com.cloudmall.product.entity.ProductSpecGroupDO;
import com.cloudmall.product.entity.ProductSpecValueDO;
import com.cloudmall.product.entity.ProductSkuSpecDO;
import com.cloudmall.product.mapper.ProductSpecGroupMapper;
import com.cloudmall.product.mapper.ProductSpecValueMapper;
import com.cloudmall.product.mapper.ProductSkuSpecMapper;
import com.cloudmall.product.service.IProductSpecService;

@Service
@RequiredArgsConstructor
public class ProductSpecService implements IProductSpecService {

    private final ProductSpecGroupMapper productSpecGroupMapper;
    private final ProductSpecValueMapper productSpecValueMapper;
    private final ProductSkuSpecMapper productSkuSpecMapper;
    private final SpecGroupConverter specGroupConverter;

    @Override
    public List<SpecGroupResp> getSpecGroups(Long productId) {
        List<ProductSpecGroupDO> groups = productSpecGroupMapper.selectList(
                Wrappers.<ProductSpecGroupDO>lambdaQuery()
                        .eq(ProductSpecGroupDO::getProductId, productId)
                        .orderByAsc(ProductSpecGroupDO::getSortOrder)
        );
        return groups.stream()
                     .map(group -> {
                         List<ProductSpecValueDO> values = productSpecValueMapper.selectList(
                                 Wrappers.<ProductSpecValueDO>lambdaQuery()
                                         .eq(ProductSpecValueDO::getGroupId, group.getId())
                                         .orderByAsc(ProductSpecValueDO::getSortOrder)
                         );
                         return specGroupConverter.toResp(group, values);
                     })
                     .toList();
    }

    @Override
    public Map<Long, List<Long>> getSkuSpecMap(List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Map.of();
        }
        List<ProductSkuSpecDO> specs = productSkuSpecMapper.selectList(
                Wrappers.<ProductSkuSpecDO>lambdaQuery()
                        .in(ProductSkuSpecDO::getSkuId, skuIds)
        );
        return specs.stream()
                    .collect(Collectors.groupingBy(
                            ProductSkuSpecDO::getSkuId,
                            Collectors.mapping(ProductSkuSpecDO::getSpecValueId, Collectors.toList())
                    ));
    }

    @Override
    public Map<Long, List<Long>> getSpecValueSkuMap(Long productId, List<Long> specValueIds) {
        if (CollUtil.isEmpty(specValueIds)) {
            return Map.of();
        }
        List<ProductSkuSpecDO> specs = productSkuSpecMapper.selectList(
                Wrappers.<ProductSkuSpecDO>lambdaQuery()
                        .in(ProductSkuSpecDO::getSpecValueId, specValueIds)
                        .inSql(ProductSkuSpecDO::getSkuId,
                                "SELECT id FROM cm_product_sku WHERE product_id = " + productId)
        );
        return specs.stream()
                    .collect(Collectors.groupingBy(
                            ProductSkuSpecDO::getSkuId,
                            Collectors.mapping(ProductSkuSpecDO::getSpecValueId, Collectors.toList())
                    ))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().size() == specValueIds.size())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
