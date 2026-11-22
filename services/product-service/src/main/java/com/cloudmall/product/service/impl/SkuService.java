package com.cloudmall.product.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.model.resp.SkuResp;
import com.cloudmall.product.convert.SkuConverter;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.product.mapper.ProductSkuMapper;
import com.cloudmall.product.mapper.ProductSkuSpecMapper;
import com.cloudmall.product.mapper.ProductSpecGroupMapper;
import com.cloudmall.product.mapper.ProductSpecValueMapper;
import com.cloudmall.product.model.entity.ProductDO;
import com.cloudmall.product.model.entity.ProductSkuDO;
import com.cloudmall.product.model.entity.ProductSkuSpecDO;
import com.cloudmall.product.model.entity.ProductSpecGroupDO;
import com.cloudmall.product.model.entity.ProductSpecValueDO;
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
        Asserts.isTrue(updated > 0, BizErrorEnum.STOCK_NOT_ENOUGH);
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
     * 批量查询上架商品的 SKU 信息。 <p>
     * 入参为空或全部 SKU 不存在、已下架时返回空列表，永不为 null。
     *
     * @param skuIds SKU 标识列表
     * @return SKU 信息列表（顺序不保证），无匹配项时为空列表
     */
    @Override
    public List<SkuResp> listSkusByIds(List<Long> skuIds) {
        List<ProductSkuDO> skus = loadSkusByIds(skuIds);
        if (CollUtil.isEmpty(skus)) {
            return List.of();
        }
        Map<Long, ProductDO> productMap = loadEnabledProductMap(skus);
        Set<Long> skuIdSet = CollStreamUtil.toSet(skus, ProductSkuDO::getId);
        Map<Long, String> specDescMap = batchBuildSpecDesc(skuIdSet);
        return assembleSkuRespList(skus, productMap, specDescMap);
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
     * 批量加载 SKU 实体，入参为空时返回空列表。
     *
     * @param skuIds SKU 标识列表
     * @return SKU 实体列表
     */
    private List<ProductSkuDO> loadSkusByIds(List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return List.of();
        }
        return productSkuMapper.selectByIds(skuIds);
    }

    /**
     * 批量加载 SKU 所属的已上架商品，按商品标识建索引。
     *
     * @param skus SKU 实体列表
     * @return 商品标识 -> 已上架商品
     */
    private Map<Long, ProductDO> loadEnabledProductMap(List<ProductSkuDO> skus) {
        Set<Long> productIds = CollStreamUtil.toSet(skus, ProductSkuDO::getProductId);
        return CollStreamUtil.toIdentityMap(productMapper.selectEnabledByIds(productIds), ProductDO::getId);
    }

    /**
     * 组装 SKU 响应列表，跳过商品不存在或已下架的 SKU。
     *
     * @param skus SKU 实体列表
     * @param productMap 商品标识 -> 已上架商品
     * @param specDescMap SKU 标识 -> 规格描述
     * @return SKU 响应列表
     */
    private List<SkuResp> assembleSkuRespList(
        List<ProductSkuDO> skus,
        Map<Long, ProductDO> productMap,
        Map<Long, String> specDescMap
    ) {
        List<SkuResp> respList = new ArrayList<>(skus.size());
        for (ProductSkuDO sku : skus) {
            ProductDO product = productMap.get(sku.getProductId());
            if (product == null) {
                continue;
            }
            respList.add(assembleSkuResp(sku, product, specDescMap.get(sku.getId())));
        }
        return respList;
    }

    /**
     * 组装单个 SKU 响应，补充商品维度的展示字段。
     *
     * @param sku SKU 实体
     * @param product 所属商品
     * @param specDesc 规格描述
     * @return SKU 响应
     */
    private SkuResp assembleSkuResp(ProductSkuDO sku, ProductDO product, String specDesc) {
        SkuResp resp = skuConverter.toResp(sku);
        resp.setProductId(sku.getProductId());
        resp.setSpecDesc(specDesc);
        resp.setCategoryId(product.getCategoryId());
        resp.setShopId(product.getShopId());
        resp.setStatus(product.getStatus());
        resp.setProductName(product.getName());
        return resp;
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
            return Map.of();
        }
        Map<Long, ProductSpecValueDO> valueMap = loadSpecValueMap(relations);
        Map<Long, ProductSpecGroupDO> groupMap = loadSpecGroupMap(valueMap);
        return CollStreamUtil.groupBy(
                relations,
                ProductSkuSpecDO::getSkuId,
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> buildSpecDesc(list, valueMap, groupMap)
                )
        );
    }

    /**
     * 批量加载 SKU 规格关联引用的规格值。
     *
     * @param relations SKU 与规格值关联列表
     * @return 规格值标识 -> 规格值
     */
    private Map<Long, ProductSpecValueDO> loadSpecValueMap(List<ProductSkuSpecDO> relations) {
        Set<Long> specValueIds = CollStreamUtil.toSet(relations, ProductSkuSpecDO::getSpecValueId);
        if (CollUtil.isEmpty(specValueIds)) {
            return Map.of();
        }
        return CollStreamUtil.toIdentityMap(
                productSpecValueMapper.selectByIds(specValueIds),
                ProductSpecValueDO::getId
        );
    }

    /**
     * 批量加载规格值所属的规格组。
     *
     * @param valueMap 规格值标识 -> 规格值
     * @return 规格组标识 -> 规格组
     */
    private Map<Long, ProductSpecGroupDO> loadSpecGroupMap(Map<Long, ProductSpecValueDO> valueMap) {
        Set<Long> groupIds = CollStreamUtil.toSet(valueMap.values(), ProductSpecValueDO::getGroupId);
        if (CollUtil.isEmpty(groupIds)) {
            return Map.of();
        }
        return CollStreamUtil.toIdentityMap(
                productSpecGroupMapper.selectByIds(groupIds),
                ProductSpecGroupDO::getId
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
        List<String> values = CollStreamUtil.toList(
                CollUtil.sort(relations, Comparator.comparingInt(r -> specValueSort(r, valueMap, groupMap))),
                r -> {
                    ProductSpecValueDO value = valueMap.get(r.getSpecValueId());
                    return value == null ? null : value.getValue();
                }
        );
        return CollUtil.join(values, "；");
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
