package com.cloudmall.product.service.impl;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.event.ProductEvent;
import com.cloudmall.product.api.event.ProductTagEnum;
import com.cloudmall.product.api.model.ImageItem;
import com.cloudmall.product.api.model.SkuItem;
import com.cloudmall.product.api.model.SpecGroupItem;
import com.cloudmall.product.api.model.SpecValueItem;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.request.ProductUpdateReq;
import com.cloudmall.product.api.response.ImageResp;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductDocResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.api.response.ProductSku;
import com.cloudmall.product.api.response.SpecGroupResp;
import com.cloudmall.product.convert.ImageConverter;
import com.cloudmall.product.convert.ProductConverter;
import com.cloudmall.product.convert.SkuConverter;
import com.cloudmall.product.convert.SpecGroupConverter;
import com.cloudmall.product.convert.SpecValueConverter;
import com.cloudmall.product.entity.BrandDO;
import com.cloudmall.product.entity.CategoryDO;
import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.entity.ProductImageDO;
import com.cloudmall.product.entity.ProductSkuDO;
import com.cloudmall.product.entity.ProductSkuSpecDO;
import com.cloudmall.product.entity.ProductSpecGroupDO;
import com.cloudmall.product.entity.ProductSpecValueDO;
import com.cloudmall.product.mapper.BrandMapper;
import com.cloudmall.product.mapper.CategoryMapper;
import com.cloudmall.product.mapper.ProductImageMapper;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.product.mapper.ProductSkuMapper;
import com.cloudmall.product.mapper.ProductSkuSpecMapper;
import com.cloudmall.product.mapper.ProductSpecGroupMapper;
import com.cloudmall.product.mapper.ProductSpecValueMapper;
import com.cloudmall.product.model.ProductSkuPrice;
import com.cloudmall.product.model.req.ProductGetReq;
import com.cloudmall.product.service.IProductImageService;
import com.cloudmall.product.service.IProductService;
import com.cloudmall.product.service.IProductSpecService;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * 商品 SPU 服务实现。
 * <p>
 * 跨表关联通过应用层多次查询组合实现，不使用数据库 JOIN。SKU 读写见 {@link SkuService}。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductSkuSpecMapper productSkuSpecMapper;
    private final ProductSpecGroupMapper productSpecGroupMapper;
    private final ProductSpecValueMapper productSpecValueMapper;
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductConverter productConverter;
    private final SpecGroupConverter specGroupConverter;
    private final SpecValueConverter specValueConverter;
    private final SkuConverter skuConverter;
    private final ImageConverter imageConverter;
    private final IProductSpecService productSpecService;
    private final IProductImageService productImageService;
    private final MQProducer mqProducer;

    /** 商品事件 Topic */
    private static final String PRODUCT_EVENTS_TOPIC = "PRODUCT_EVENTS";

    /**
     * 获取相关数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @Override
    public ProductResp get(ProductGetReq req) {
        ProductDO product = Asserts.requireNonNull(productMapper.selectByGetReq(req));
        return assembleProductResp(product);
    }

    @NonNull
    private ProductResp assembleProductResp(ProductDO product) {
        ProductResp resp = productConverter.toResp(product);
        BrandDO brand = Asserts.requireNonNull(brandMapper.selectById(product.getBrandId()));
        ProductImageDO img = Asserts.requireNonNull(productImageMapper.selectMainImage(product.getId()));
        CategoryDO categoryDO = Asserts.requireNonNull(categoryMapper.selectById(product.getCategoryId()));
        BigDecimal lowestPrice = productSkuMapper.selectLowestPrice(product.getId());

        resp.setPrice(lowestPrice);
        resp.setBrand(brand.getName());
        resp.setCategoryId(categoryDO.getId());
        resp.setCategoryName(categoryDO.getName());
        resp.setImage(img.getUrl());
        return resp;
    }

    /**
     * 游标拉取商品搜索文档，支持 ID 游标与时间游标两种模式。 <p>
     * 不过滤商品状态（下架商品 status 透传， 由消费方决定写入或丢弃）。文档不含 sales， 避
     * 免覆盖 ES 中的销量。
     *
     * @param req 游标分页参数（cursor / cursorTime 二选一，见
     *            {@link CursorReq}）
     * @return 游标分页结果（两种游标均回填，调用方按模式读取）
     */
    @Override
    public CursorResult<ProductDocResp> cursorPage(CursorReq req) {
        int size = ObjectUtil.defaultIfNull(req.getSize(), 10);
        List<ProductDO> records = productMapper.selectSearchDocsByCursor(req, size + 1);
        boolean hasMore = records.size() > size;
        if (hasMore) {
            records = records.subList(0, size);
        }
        if (CollUtil.isEmpty(records)) {
            return CursorResult.<ProductDocResp>builder()
                               .items(List.of())
                               .cursor(0L)
                               .cursorTime(null)
                               .hasMore(false)
                               .build();
        }

        List<ProductDocResp> docs = assembleSearchDocs(records);
        ProductDO last = records.get(records.size() - 1);
        Long nextCursor = hasMore ? last.getId() : 0L;
        LocalDateTime nextCursorTime = hasMore ? last.getUpdateTime() : null;
        return CursorResult.<ProductDocResp>builder()
                           .items(docs)
                           .cursor(nextCursor)
                           .cursorTime(nextCursorTime)
                           .hasMore(hasMore)
                           .build();
    }

    /**
     * 按商品 ID 组装单个搜索文档。
     *
     * @param id SPU ID
     * @return 搜索文档，商品不存在返回 null
     */
    @Override
    public ProductDocResp searchDoc(Long id) {
        ProductDO product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        Set<Long> brandIds = singletonOrEmpty(product.getBrandId());
        Set<Long> categoryIds = singletonOrEmpty(product.getCategoryId());
        BigDecimal lowestPrice = productSkuMapper.selectLowestPrice(id);
        return assembleSearchDoc(
                product,
                batchQuery(brandIds, brandMapper::selectByIds, BrandDO::getId),
                batchQuery(categoryIds, categoryMapper::selectByIds, CategoryDO::getId),
                batchQuery(
                        Set.of(id),
                        productImageMapper::selectMainImagesByProductIds,
                        ProductImageDO::getProductId
                ),
                singletonMapOrEmpty(id, lowestPrice)
        );
    }

    private static Set<Long> singletonOrEmpty(Long value) {
        return value == null ? Set.of() : Set.of(value);
    }

    /**
     * 按分类 ID 集合查询已上架商品（首页分类展示用）。 <p>
     * 分类 ID 集合可包含指定分类及其子分类，仅查询已上架商品并按创建时间倒序。
     *
     * @param categoryIds 分类 ID 集合
     * @param limit 查询条数上限（1~50，默认 10）
     * @return 商品搜索文档列表
     */
    @Override
    public List<ProductDocResp> listByCategoryIds(Collection<Long> categoryIds, Integer limit) {
        int size = Math.min(Math.max(ObjectUtil.defaultIfNull(limit, 10), 1), 50);
        List<ProductDO> records = productMapper.selectEnabledByCategoryIds(categoryIds, size);
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }
        return assembleSearchDocs(records);
    }

    /**
     * 批量组装商品搜索文档（SPU + SKU 最低价 + 分类/品牌名 + 主图）。
     *
     * @param records 商品记录
     * @return 商品搜索文档列表
     */
    private List<ProductDocResp> assembleSearchDocs(List<ProductDO> records) {
        Set<Long> brandIds = collectNonNull(records, ProductDO::getBrandId);
        Set<Long> categoryIds = collectNonNull(records, ProductDO::getCategoryId);
        Set<Long> productIds = records.stream()
                                      .map(ProductDO::getId)
                                      .collect(Collectors.toSet());

        Map<Long, BrandDO> brandMap = batchQuery(brandIds, brandMapper::selectByIds, BrandDO::getId);
        Map<Long, CategoryDO> categoryMap = batchQuery(
                categoryIds,
                categoryMapper::selectByIds,
                CategoryDO::getId
        );
        Map<Long, ProductImageDO> imageMap = batchQuery(
                productIds,
                productImageMapper::selectMainImagesByProductIds,
                ProductImageDO::getProductId
        );
        Map<Long, BigDecimal> lowestPriceMap = productSkuMapper
                .selectLowestPrices(productIds)
                .stream()
                .collect(Collectors.toMap(ProductSkuPrice::productId, ProductSkuPrice::price));

        return records.stream()
                      .map(p -> assembleSearchDoc(p, brandMap, categoryMap, imageMap, lowestPriceMap))
                      .toList();
    }

    private static <K, V> Map<K, V> singletonMapOrEmpty(K key, V value) {
        return value == null ? Map.of() : Map.of(key, value);
    }

    private static Set<Long> collectNonNull(List<ProductDO> records, Function<ProductDO, Long> getter) {
        return records.stream()
                      .map(getter)
                      .filter(Objects::nonNull)
                      .collect(Collectors.toSet());
    }

    private static <T> Map<Long, T> batchQuery(
        Set<Long> ids,
        Function<Set<Long>, List<T>> query,
        Function<T, Long> idGetter
    ) {
        if (CollUtil.isEmpty(ids)) {
            return Map.of();
        }
        List<T> list = query.apply(ids);
        if (CollUtil.isEmpty(list)) {
            return Map.of();
        }
        return list.stream().collect(Collectors.toMap(idGetter, Function.identity()));
    }

    private static ProductDocResp assembleSearchDoc(
        ProductDO product,
        Map<Long, BrandDO> brandMap,
        Map<Long, CategoryDO> categoryMap,
        Map<Long, ProductImageDO> imageMap,
        Map<Long, BigDecimal> lowestPriceMap
    ) {
        BrandDO brand = brandMap.get(product.getBrandId());
        CategoryDO category = categoryMap.get(product.getCategoryId());
        ProductImageDO image = imageMap.get(product.getId());
        return ProductDocResp.builder()
                             .id(product.getId())
                             .name(product.getName())
                             .spuCode(product.getSpuCode())
                             .description(product.getDescription())
                             .categoryId(product.getCategoryId())
                             .categoryName(category != null ? category.getName() : null)
                             .brandId(product.getBrandId())
                             .brandName(brand != null ? brand.getName() : null)
                             .shopId(product.getShopId())
                             .status(product.getStatus() != null ? product.getStatus().getValue() : null)
                             .priceMin(lowestPriceMap.get(product.getId()))
                             .mainImage(image != null ? image.getUrl() : null)
                             .createTime(product.getCreateTime())
                             .updateTime(product.getUpdateTime())
                             .build();
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 结果标识或数值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductCreateReq req, Long shopId) {
        validateBrandAndCategory(req.getBrandId(), req.getCategoryId());
        validateSkuTopology(req.getSpecGroups(), req.getSkus());
        validatePrices(req.getSkus());

        ProductDO productDO = saveProductWithImages(req, shopId);
        List<List<Long>> specValueIds = saveSpecs(productDO.getId(), req.getSpecGroups());
        saveSkus(productDO.getId(), req, specValueIds);
        syncEsEvent(productDO.getId(), productDO, ProductTagEnum.CREATED);
        return productDO.getId();
    }

    /**
     * 更新商品（SPU 基本信息 + 规格 + SKU + 图片），并发布 ProductUpdated
     * 事件。 <p>
     * 差异合并语义：按规格组合指纹匹配，已存在 SKU 保留主键/编码/库存， 仅刷新销售字段；新增规格组
     * 合插入新 SKU。
     *
     * @param id SPU ID
     * @param req 更新请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductUpdateReq req) {
        ProductDO product = Asserts.requireNonNull(
                productMapper.selectById(id),
                BizError.PRODUCT_OR_SHOP_NOT_EXIST
        );
        validateBrandAndCategory(req.getBrandId(), req.getCategoryId());
        validateSkuTopology(req.getSpecGroups(), req.getSkus());
        validatePrices(req.getSkus());

        applyBasicFields(product, req);
        replaceImages(product, req.getImages());
        List<List<Long>> specValueIds = replaceSpecs(id, req.getSpecGroups());
        replaceSkus(id, req, specValueIds);

        syncEsEvent(id, product, ProductTagEnum.UPDATED);
    }

    /**
     * 发布商品搜索同步事件（事件载荷与消息 tag 均携带事件类型）。
     *
     * @param id SPU ID
     * @param product SPU 实体
     * @param tagEnum 商品事件类型
     */
    private void syncEsEvent(Long id, ProductDO product, ProductTagEnum tagEnum) {
        ProductEvent event = ProductEvent.builder()
                                         .productId(id)
                                         .spuCode(product.getSpuCode())
                                         .build();

        mqProducer.sendInTransaction(PRODUCT_EVENTS_TOPIC, tagEnum.getTag(), event, String.valueOf(id));
    }

    /**
     * 刷新 SPU 基本信息并落库。
     *
     * @param product SPU 实体
     * @param req 更新请求
     */
    private void applyBasicFields(ProductDO product, ProductUpdateReq req) {
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setCategoryId(req.getCategoryId());
        product.setBrandId(req.getBrandId());
        productMapper.updateById(product);
    }

    /**
     * 全量替换商品图片：物理删除旧图后按请求重建。
     *
     * @param product SPU 实体
     * @param images 图片请求模型列表
     */
    private void replaceImages(ProductDO product, List<ImageItem> images) {
        productImageMapper.deleteByProductId(product.getId());
        saveImages(images, product.getId());
    }

    /**
     * 差异合并规格组与规格值，返回 [组下标][值下标] -> 规格值主键 的二维映射。 <p>
     * 按归一化名称匹配：已存在的组/值复用主键并刷新排序，新增则插入； 请求中已不存在的组/值软删除（被保
     * 留 SKU 引用的值不会进入删除集合）。
     *
     * @param productId SPU ID
     * @param groups 规格组请求模型列表
     * @return 二维映射，外层下标为规格组序号，内层下标为组内规格值序号
     */
    private List<List<Long>> replaceSpecs(Long productId, List<SpecGroupItem> groups) {
        Map<String, ProductSpecGroupDO> existingGroups = productSpecGroupMapper
                .selectByProductId(productId)
                .stream()
                .collect(Collectors.toMap(g -> normalize(g.getName()), Function.identity()));
        Set<String> newGroupNames = new HashSet<>();
        List<List<Long>> specValueIds = new ArrayList<>(groups.size());

        for (int g = 0; g < groups.size(); g++) {
            SpecGroupItem groupItem = groups.get(g);
            String groupName = normalize(groupItem.getName());
            newGroupNames.add(groupName);

            ProductSpecGroupDO group = existingGroups.get(groupName);
            if (group == null) {
                group = specGroupConverter.toDO(groupItem, productId);
                group.setName(groupName);
                group.setSort(g);
                productSpecGroupMapper.insert(group);
            } else {
                group.setSort(g);
                productSpecGroupMapper.updateById(group);
            }

            Map<String, ProductSpecValueDO> existingValues = productSpecValueMapper
                    .selectByGroupId(group.getId())
                    .stream()
                    .collect(Collectors.toMap(v -> normalize(v.getValue()), Function.identity()));
            Set<String> newValueNames = new HashSet<>();
            List<Long> idsOfThisGroup = new ArrayList<>(groupItem.getValues().size());

            for (int v = 0; v < groupItem.getValues().size(); v++) {
                SpecValueItem valueItem = groupItem.getValues().get(v);
                String valueName = normalize(valueItem.getValue());
                newValueNames.add(valueName);

                ProductSpecValueDO value = existingValues.get(valueName);
                if (value == null) {
                    value = specValueConverter.toDO(valueItem, group.getId());
                    value.setValue(valueName);
                    value.setSort(v);
                    productSpecValueMapper.insert(value);
                } else {
                    value.setSort(v);
                    if (StrUtil.isNotBlank(valueItem.getImage())) {
                        value.setImage(valueItem.getImage());
                    }
                    productSpecValueMapper.updateById(value);
                }
                idsOfThisGroup.add(value.getId());
            }

            deleteOrphanValues(existingValues, newValueNames);
            specValueIds.add(idsOfThisGroup);
        }

        deleteOrphanGroups(existingGroups, newGroupNames);
        return specValueIds;
    }

    /**
     * 软删除请求中已不存在的规格值。
     *
     * @param existingValues 更新前组内规格值，按归一化名称索引
     * @param newValueNames 请求中的规格值名称集合
     */
    private void deleteOrphanValues(Map<String, ProductSpecValueDO> existingValues, Set<String> newValueNames) {
        Set<Long> orphanIds = existingValues.entrySet()
                                            .stream()
                                            .filter(entry -> !newValueNames.contains(entry.getKey()))
                                            .map(entry -> entry.getValue().getId())
                                            .collect(Collectors.toSet());
        productSpecValueMapper.deleteValues(orphanIds);
    }

    /**
     * 软删除请求中已不存在的规格组。
     *
     * @param existingGroups 更新前规格组，按归一化名称索引
     * @param newGroupNames 请求中的规格组名称集合
     */
    private void deleteOrphanGroups(Map<String, ProductSpecGroupDO> existingGroups, Set<String> newGroupNames) {
        Set<Long> orphanIds = existingGroups.entrySet()
                                            .stream()
                                            .filter(entry -> !newGroupNames.contains(entry.getKey()))
                                            .map(entry -> entry.getValue().getId())
                                            .collect(Collectors.toSet());
        productSpecGroupMapper.deleteGroups(orphanIds);
    }

    /**
     * 差异合并 SKU 及 SKU-规格值关联表。 <p>
     * 按规格组合指纹匹配：已存在 SKU 保留主键/编码/库存，仅刷新价格等销售字段； 新增组合插入新
     * SKU（库存取请求值）。旧 SKU 的关联行物理删除后按新组合重建， 请求中已不存在的 SKU 软删
     * 除。
     *
     * @param productId SPU ID
     * @param req 更新请求
     * @param specValueIds 规格值主键的二维映射
     */
    private void replaceSkus(Long productId, ProductUpdateReq req, List<List<Long>> specValueIds) {
        List<SpecGroupItem> groups = req.getSpecGroups();
        List<ProductSkuDO> existingSkus = productSkuMapper.selectByProductId(productId);
        Map<String, ProductSkuDO> existingByHash = existingSkus
                .stream()
                .filter(s -> StrUtil.isNotBlank(s.getSpecHash()))
                .collect(Collectors.toMap(ProductSkuDO::getSpecHash, Function.identity()));

        List<ProductSkuDO> newSkus = new ArrayList<>();
        List<ProductSkuSpecDO> relations = new ArrayList<>();

        for (SkuItem skuItem : req.getSkus()) {
            List<Long> realIds = resolveSpecValueIds(groups, skuItem, specValueIds);
            String hash = specHash(realIds);

            ProductSkuDO existing = existingByHash.get(hash);
            ProductSkuDO sku;
            if (existing != null) {
                existing.setPrice(skuItem.getPrice());
                existing.setMarketPrice(skuItem.getMarketPrice());
                existing.setWeight(skuItem.getWeight());
                existing.setImage(skuItem.getImage());
                productSkuMapper.updateById(existing);
                sku = existing;
            } else {
                sku = skuConverter.toDO(
                        skuItem,
                        productId,
                        String.format("SKU%s", IdUtil.getSnowflakeNextIdStr())
                );
                sku.setSpecHash(hash);
                productSkuMapper.insert(sku);
            }
            newSkus.add(sku);

            for (Long specValueId : realIds) {
                relations.add(ProductSkuSpecDO.builder()
                                              .skuId(sku.getId())
                                              .specValueId(specValueId)
                                              .build());
            }
        }

        rewriteSkuSpecRelations(existingSkus, relations);
        softDeleteRemovedSkus(existingSkus, newSkus);
    }

    /**
     * 重建 SKU-规格值关联：物理删除旧 SKU 的全部关联行后批量写入新关联。
     *
     * @param existingSkus 更新前 SKU 列表
     * @param relations 新关联行列表
     */
    private void rewriteSkuSpecRelations(List<ProductSkuDO> existingSkus, List<ProductSkuSpecDO> relations) {
        List<Long> oldSkuIds = existingSkus.stream()
                                           .map(ProductSkuDO::getId)
                                           .toList();
        productSkuSpecMapper.deleteBySkuIds(oldSkuIds);
        productSkuSpecMapper.insert(relations);
    }

    /**
     * 软删除请求中已不存在的 SKU。
     *
     * @param existingSkus 更新前 SKU 列表
     * @param newSkus 更新后的 SKU 列表（含保留与新增）
     */
    private void softDeleteRemovedSkus(List<ProductSkuDO> existingSkus, List<ProductSkuDO> newSkus) {
        Set<String> newHashes = newSkus.stream()
                                       .map(ProductSkuDO::getSpecHash)
                                       .collect(Collectors.toSet());
        List<Long> removedIds = existingSkus.stream()
                                            .filter(s -> !newHashes.contains(s.getSpecHash()))
                                            .map(ProductSkuDO::getId)
                                            .toList();
        if (CollUtil.isEmpty(removedIds)) {
            return;
        }
        // 清空 spec_hash 释放 uk_product_spec 唯一槽位，避免该组合后续被重新添加时撞软删除行
        productSkuMapper.clearSpecHashByIds(removedIds);
        productSkuMapper.deleteByIds(removedIds);
    }

    /**
     * 删除商品（软删除），并发布 ProductDeleted 事件。
     *
     * @param id SPU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProductDO product = Asserts.requireNonNull(
                productMapper.selectById(id),
                BizError.PRODUCT_OR_SHOP_NOT_EXIST
        );
        Asserts.isTrue(productMapper.deleteById(id) > 0, BizError.SPU_DELETE_FAILED);

        syncEsEvent(id, product, ProductTagEnum.DELETED);
    }

    /**
     * 校验品牌与分类存在。
     *
     * @param brandId 品牌 ID
     * @param categoryId 分类 ID
     */
    private void validateBrandAndCategory(Long brandId, Long categoryId) {
        Asserts.notNull(brandMapper.selectById(brandId), BizError.BRAND_NOT_EXIST);
        Asserts.notNull(categoryMapper.selectById(categoryId), BizError.CATEGORY_NOT_EXIST_OR_NO_ENABLE);
    }

    private ProductDO saveProductWithImages(ProductCreateReq req, Long shopId) {
        ProductDO product = saveProduct(req, shopId);
        saveImages(req.getImages(), product.getId());
        return product;
    }

    private ProductDO saveProduct(ProductCreateReq req, Long shopId) {
        ProductDO product = productConverter.toDO(req);
        product.setSpuCode(String.format("SPU%s", IdUtil.getSnowflakeNextIdStr()));
        product.setShopId(shopId);
        productMapper.insert(product);
        return product;
    }

    /**
     * 落商品图片。 <p>
     * 主图与排序均按下标推导：首张为主图，sort 取下标。
     *
     * @param images 图片请求模型列表
     * @param productId SPU ID
     */
    private void saveImages(List<ImageItem> images, Long productId) {
        List<ProductImageDO> imageDOList = new ArrayList<>(images.size());
        for (int i = 0; i < images.size(); i++) {
            ProductImageDO image = imageConverter.toDO(images.get(i), productId);
            image.setSort(i);
            image.setIsMain(i == 0);
            imageDOList.add(image);
        }
        productImageMapper.insert(imageDOList);
    }

    /**
     * 落规格组与规格值，同时返回 [组下标][值下标] -> 规格值主键 的二维映射。 <p>
     * 这张映射是把请求里的 {@code specValues} 翻译成外键的唯一依据，方法返回后即丢弃，
     * 不落库。 规格组与规格值的 sort 取各自的数组下标。 <p>
     * 逐条插入而非批量：规格值不会太多
     *
     * @param productId SPU ID
     * @param groups 规格组请求模型列表
     * @return 二维映射，外层下标为规格组序号，内层下标为组内规格值序号
     */
    private List<List<Long>> saveSpecs(Long productId, List<SpecGroupItem> groups) {
        List<List<Long>> specValueIds = new ArrayList<>(groups.size());

        for (int g = 0; g < groups.size(); g++) {
            SpecGroupItem groupItem = groups.get(g);

            ProductSpecGroupDO group = specGroupConverter.toDO(groupItem, productId);
            group.setName(normalize(groupItem.getName()));
            group.setSort(g);
            productSpecGroupMapper.insert(group);

            List<SpecValueItem> values = groupItem.getValues();
            List<Long> idsOfThisGroup = new ArrayList<>(values.size());
            for (int v = 0; v < values.size(); v++) {
                ProductSpecValueDO specValue = specValueConverter.toDO(values.get(v), group.getId());
                specValue.setValue(normalize(values.get(v).getValue()));
                specValue.setSort(v);
                productSpecValueMapper.insert(specValue);

                // 位置 v 与主键一一对应
                idsOfThisGroup.add(specValue.getId());
            }
            specValueIds.add(idsOfThisGroup);
        }
        return specValueIds;
    }

    /**
     * 落 SKU 及 SKU-规格值关联表。 <p>
     * 用 {@link #saveSpecs} 返回的映射把每个 SKU 的
     * {@code specValues} 翻译成规格值主键： 第 g 位的值只在第 g 个规格组的候选里
     * 查找，跨组引用已在 {@link #validateSkuTopology} 拦下。
     *
     * @param productId SPU ID
     * @param req 创建请求
     * @param specValueIds 规格值主键的二维映射
     */
    private void saveSkus(Long productId, ProductCreateReq req, List<List<Long>> specValueIds) {
        List<SpecGroupItem> groups = req.getSpecGroups();
        List<ProductSkuSpecDO> relations = new ArrayList<>();

        for (SkuItem skuItem : req.getSkus()) {
            List<Long> realIds = resolveSpecValueIds(groups, skuItem, specValueIds);

            ProductSkuDO sku = skuConverter.toDO(
                    skuItem,
                    productId,
                    String.format("SKU%s", IdUtil.getSnowflakeNextIdStr())
            );
            sku.setSpecHash(specHash(realIds));
            // 逐条插入以回填自增 ID
            productSkuMapper.insert(sku);

            for (Long specValueId : realIds) {
                ProductSkuSpecDO skuSpec = ProductSkuSpecDO.builder()
                                                           .skuId(sku.getId())
                                                           .specValueId(specValueId)
                                                           .build();
                relations.add(skuSpec);
            }
        }
        // 关联表无自增 ID 需要回填，可以安全批量写入
        productSkuSpecMapper.insert(relations);
    }

    /**
     * 把 SKU 的规格值名称翻译成规格值主键。 <p>
     * 第 g 位的值只在第 g 个规格组的候选里匹配，跨组引用已在
     * {@link #validateSkuTopology} 拦下。
     *
     * @param groups 规格组请求模型列表
     * @param skuItem SKU 请求模型
     * @param specValueIds 规格值主键的二维映射
     * @return 该 SKU 关联的规格值主键列表
     */
    private List<Long> resolveSpecValueIds(
        List<SpecGroupItem> groups,
        SkuItem skuItem,
        List<List<Long>> specValueIds
    ) {
        List<Long> realIds = new ArrayList<>(groups.size());
        for (int g = 0; g < groups.size(); g++) {
            String target = normalize(skuItem.getSpecValues().get(g));
            int v = indexOfValue(groups.get(g).getValues(), target);
            realIds.add(specValueIds.get(g).get(v));
        }
        return realIds;
    }

    /**
     * 获取商品详情。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public ProductDetailResp detail(Long id) {
        ProductDO product = Asserts.requireNonNull(
                productMapper.selectById(id),
                BizError.PRODUCT_DETAIL_NOT_FOUND
        );
        BrandDO brand = brandMapper.selectById(product.getBrandId());
        CategoryDO category = categoryMapper.selectById(product.getCategoryId());
        List<ImageResp> images = productImageService.listByProductId(id);
        List<SpecGroupResp> specGroups = productSpecService.getSpecGroups(id);
        List<ProductSkuDO> skus = productSkuMapper.selectByProductId(id);
        List<Long> skuIds = skus.stream()
                                .map(ProductSkuDO::getId)
                                .collect(Collectors.toList());
        Map<Long, List<Long>> skuSpecMap = productSpecService.getSkuSpecMap(skuIds);
        List<ProductSku> skuResponses = skuConverter.toRespList(skus, skuSpecMap);

        return productConverter.toDetailResp(product, brand, category, images, skuResponses, specGroups);
    }

    /**
     * 校验规格组与 SKU 的拓扑结构。 <p>
     * 逐项校验：组名不重复、组内规格值不重复、每个 SKU 为每个规格组恰好指定一个值、 第 g 位的值确
     * 实存在于第 g 个规格组、SKU 之间规格组合不重复。 <p>
     * 按组隔离查找是这里的关键：{@code ["M", "L"]} 这种请求里，"M" 虽是合法规格值但
     * 属于尺码组， 第 0 位只在颜色组的候选里查，因此查不到而被拒绝。
     *
     * @param groups 规格组请求模型列表
     * @param skus SKU 请求模型列表
     */
    private void validateSkuTopology(List<SpecGroupItem> groups, List<SkuItem> skus) {
        // 组名不重复、组内规格值不重复
        Set<String> groupNames = new HashSet<>();
        for (SpecGroupItem group : groups) {
            Asserts.isTrue(groupNames.add(normalize(group.getName())), BizError.SPEC_VALUE_INVALID);
            Set<String> values = new HashSet<>();
            for (SpecValueItem value : group.getValues()) {
                Asserts.isTrue(values.add(normalize(value.getValue())), BizError.SPEC_VALUE_INVALID);
            }
        }

        Set<List<String>> seen = new HashSet<>();
        for (SkuItem sku : skus) {
            List<String> specValues = sku.getSpecValues();

            // 每个规格组恰好一个值
            Asserts.notEmpty(specValues, BizError.SKU_SPECS_CANNOT_BE_EMPTY);
            Asserts.isEqual(specValues.size(), groups.size(), BizError.SKU_SPEC_NOT_MATCH);

            // 按组隔离查找，第 g 位的值只在第 g 组的候选里匹配
            List<String> normalized = new ArrayList<>(specValues.size());
            for (int g = 0; g < groups.size(); g++) {
                String target = normalize(specValues.get(g));
                Asserts.isTrue(
                        indexOfValue(groups.get(g).getValues(), target) >= 0,
                        BizError.SKU_SPEC_NOT_MATCH
                );
                normalized.add(target);
            }

            // 组合唯一。位置已固定，直接比较归一化后的列表，无需排序
            Asserts.isTrue(seen.add(normalized), BizError.SKU_SPEC_NOT_MATCH);
        }
    }

    /**
     * 校验 SKU 价格。市场价存在时，售价不得高于市场价。
     *
     * @param skus SKU 请求模型列表
     */
    private void validatePrices(List<SkuItem> skus) {
        for (SkuItem sku : skus) {
            if (sku.getMarketPrice() != null) {
                Asserts.isTrue(sku.getPrice().compareTo(sku.getMarketPrice()) <= 0, BizError.INVALID_PARAM);
            }
        }
    }
    /**
     * 归一化文本，用于规格组名与规格值的匹配和落库。 <p>
     * 全角转半角（NFKC）+ 去首尾空白 + 折叠内部连续空白， 避免 "红 色" / "Ｓ" 这类写法
     * 绕过匹配后产生重复规格值。
     *
     * @param text 原始文本
     * @return 归一化后的文本，入参为 null 时返回空串
     */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFKC).replaceAll("\\s+", " ");
    }

    /**
     * 在单个规格组的候选规格值中查找目标值的下标。
     *
     * @param candidates 该规格组下的所有规格值
     * @param target 已归一化的目标值
     * @return 命中下标，未命中返回 -1
     */
    private static int indexOfValue(List<SpecValueItem> candidates, String target) {
        for (int i = 0; i < candidates.size(); i++) {
            if (normalize(candidates.get(i).getValue()).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将规格值 ID 升序拼接为组合指纹，使组合唯一性与传入顺序无关。 <p>
     * 落到 {@code cm_product_sku.spec_hash}，配合
     * {@code uk_product_spec} 挡住并发重复提交。
     *
     * @param specValueIds 该 SKU 关联的规格值 ID
     * @return 形如 {@code "102_105"} 的指纹
     */
    private static String specHash(List<Long> specValueIds) {
        return specValueIds.stream()
                           .sorted()
                           .map(String::valueOf)
                           .collect(Collectors.joining("_"));
    }

}
