package com.cloudmall.product.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.response.SkuResp;
import com.cloudmall.product.convert.SkuConverter;
import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.entity.ProductSkuDO;
import com.cloudmall.product.entity.ProductSkuSpecDO;
import com.cloudmall.product.entity.ProductSpecGroupDO;
import com.cloudmall.product.entity.ProductSpecValueDO;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.product.mapper.ProductSkuMapper;
import com.cloudmall.product.mapper.ProductSkuSpecMapper;
import com.cloudmall.product.mapper.ProductSpecGroupMapper;
import com.cloudmall.product.mapper.ProductSpecValueMapper;
import com.cloudmall.product.service.IProductSpecService;
import com.cloudmall.product.service.ISkuService;

/**
 * 商品 SKU 服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class SkuService implements ISkuService {

    private final ProductSkuMapper productSkuMapper;
    private final ProductMapper productMapper;
    private final ProductSkuSpecMapper productSkuSpecMapper;
    private final ProductSpecValueMapper productSpecValueMapper;
    private final ProductSpecGroupMapper productSpecGroupMapper;
    private final SkuConverter skuConverter;
    private final IProductSpecService productSpecService;

    /**
     * 扣减 SKU 库存。 <p>
     * quantity 为正数，条件更新（stock >= quantity）防止超卖；quantity
     * 为 0 直接成功。
     *
     * @param skuId SKU 标识
     * @param quantity 扣减数量（正数）
     * @return 是否成功
     * @throws BizException 库存不足或 SKU 不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deductStock(Long skuId, Integer quantity) {
        Asserts.requireNonNull(productSkuMapper.selectById(skuId));
        if (quantity == 0) {
            return Boolean.TRUE;
        }
        int updated = productSkuMapper.reserveStock(skuId, quantity);
        Asserts.isTrue(updated > 0, BizError.STOCK_NOT_ENOUGH);
        return Boolean.TRUE;
    }

    /**
     * 回滚 SKU 库存（增加库存）。 <p>
     * quantity 为正数，无条件加回；quantity 为 0 直接成功。
     *
     * @param skuId SKU 标识
     * @param quantity 回滚数量（正数）
     * @return 是否成功
     * @throws BizException SKU 不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean releaseStock(Long skuId, Integer quantity) {
        Asserts.requireNonNull(productSkuMapper.selectById(skuId));
        if (quantity == 0) {
            return Boolean.TRUE;
        }
        productSkuMapper.releaseStock(skuId, quantity);
        return Boolean.TRUE;
    }

    /**
     * 查询数据列表。
     *
     * @param skuIds SKU 标识列表
     * @return 数据列表
     */
    @Override
    public List<SkuResp> listSkusByIds(List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        List<ProductSkuDO> skus = productSkuMapper.selectByIds(skuIds);
        if (CollUtil.isEmpty(skus)) {
            return Collections.emptyList();
        }

        // 批量查询已上架商品（取店铺 ID）
        Set<Long> productIds = skus.stream()
                                   .map(ProductSkuDO::getProductId)
                                   .collect(Collectors.toSet());
        List<ProductDO> products = productMapper.selectEnabledByIds(productIds);
        Map<Long, ProductDO> productMap = products.stream()
                                                  .collect(
                                                          Collectors.toMap(
                                                                  ProductDO::getId,
                                                                  Function.identity()
                                                          )
                                                  );

        // 批量查询规格描述（skuId -> "红色；XL"）
        Set<Long> skuIdSet = skus.stream()
                                 .map(ProductSkuDO::getId)
                                 .collect(Collectors.toSet());
        Map<Long, String> specDescMap = batchBuildSpecDesc(skuIdSet);

        List<SkuResp> respList = new ArrayList<>(skus.size());
        for (ProductSkuDO sku : skus) {
            ProductDO product = productMap.get(sku.getProductId());
            if (product == null) {
                continue;
            }
            SkuResp resp = skuConverter.toResp(sku);
            resp.setProductId(sku.getProductId());
            resp.setSpecDesc(specDescMap.get(sku.getId()));
            resp.setCategoryId(product.getCategoryId());
            resp.setShopId(product.getShopId());
            resp.setStatus(product.getStatus());
            resp.setProductName(product.getName());
            respList.add(resp);
        }
        return respList;
    }

    /**
     * 匹配并处理相关数据。
     *
     * @param productId 商品标识
     * @param specValueIds 规格值标识列表
     * @return 处理结果
     */
    @Override
    public SkuResp matchSkuBySpec(Long productId, List<Long> specValueIds) {
        if (CollUtil.isEmpty(specValueIds)) {
            return null;
        }
        Map<Long, List<Long>> matched = productSpecService.getSpecValueSkuMap(productId, specValueIds);
        if (matched.size() != 1) {
            return null;
        }
        Long skuId = matched.keySet()
                            .iterator()
                            .next();
        List<SkuResp> skus = listSkusByIds(List.of(skuId));
        return CollUtil.isEmpty(skus) ? null : skus.get(0);
    }

    /**
     * 批量查询 SKU 的规格描述。 <p>
     * 规格值与规格组分属两张关联表，分三步批量查询后按规格组排序拼接。
     *
     * @param skuIds SKU ID 集合
     * @return skuId -> 规格描述（如 "红色；XL"）
     */
    private Map<Long, String> batchBuildSpecDesc(Set<Long> skuIds) {
        List<ProductSkuSpecDO> relations = productSkuSpecMapper.selectBySkuIds(skuIds);
        if (CollUtil.isEmpty(relations)) {
            return Collections.emptyMap();
        }

        Set<Long> specValueIds = relations.stream()
                                          .map(ProductSkuSpecDO::getSpecValueId)
                                          .collect(Collectors.toSet());
        List<ProductSpecValueDO> values = productSpecValueMapper.selectByIds(specValueIds);
        Map<Long, ProductSpecValueDO> valueMap = values.stream()
                                                       .collect(
                                                               Collectors.toMap(
                                                                       ProductSpecValueDO::getId,
                                                                       Function.identity()
                                                               )
                                                       );

        Set<Long> groupIds = valueMap.values()
                                     .stream()
                                     .map(ProductSpecValueDO::getGroupId)
                                     .collect(Collectors.toSet());
        List<ProductSpecGroupDO> groups = productSpecGroupMapper.selectByIds(groupIds);
        Map<Long, ProductSpecGroupDO> groupMap = groups.stream()
                                                       .collect(
                                                               Collectors.toMap(
                                                                       ProductSpecGroupDO::getId,
                                                                       Function.identity()
                                                               )
                                                       );

        return relations.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ProductSkuSpecDO::getSkuId,
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                list -> buildSpecDesc(list, valueMap, groupMap)
                                        )
                                )
                        );
    }

    /**
     * 将单个 SKU 的规格值按规格组排序拼接为描述。
     *
     * @param relations SKU 与规格值关联列表
     * @param valueMap 规格值主键 -> 规格值
     * @param groupMap 规格组主键 -> 规格组
     * @return 规格描述，如 "红色；XL"
     */
    private String buildSpecDesc(
        List<ProductSkuSpecDO> relations,
        Map<Long, ProductSpecValueDO> valueMap,
        Map<Long, ProductSpecGroupDO> groupMap
    ) {
        return relations
                .stream()
                .sorted(Comparator.comparingInt(r -> specValueSort(r, valueMap, groupMap)))
                .map(
                        r -> {
                            ProductSpecValueDO value = valueMap.get(r.getSpecValueId());
                            return value == null ? null : value.getValue();
                        }
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining("；"));
    }

    /**
     * 计算规格值所属规格组的排序值，用于规格描述的有序拼接。
     *
     * @param relation SKU 与规格值关联
     * @param valueMap 规格值主键 -> 规格值
     * @param groupMap 规格组主键 -> 规格组
     * @return 规格组排序值，数据缺失时按 0 处理
     */
    private int specValueSort(
        ProductSkuSpecDO relation,
        Map<Long, ProductSpecValueDO> valueMap,
        Map<Long, ProductSpecGroupDO> groupMap
    ) {
        ProductSpecValueDO value = valueMap.get(relation.getSpecValueId());
        if (value == null) {
            return 0;
        }
        ProductSpecGroupDO group = groupMap.get(value.getGroupId());
        return group == null ? 0 : ObjectUtil.defaultIfNull(group.getSort(), 0);
    }
}
