package com.cloudmall.product.service.impl;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.model.ImageItem;
import com.cloudmall.product.api.model.SkuItem;
import com.cloudmall.product.api.model.SpecGroupItem;
import com.cloudmall.product.api.model.SpecValueItem;
import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.ImageResp;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductResp;
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
import com.cloudmall.product.model.req.ProductPageReq;
import com.cloudmall.product.service.IProductImageService;
import com.cloudmall.product.service.IProductService;
import com.cloudmall.product.service.IProductSpecService;

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
        ProductImageDO img = Asserts.requireNonNull(productImageMapper.selectById(product.getImageId()));
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
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<ProductResp> page(ProductPageReq req) {
        IPage<ProductDO> page = productMapper.selectProductPage(req);

        List<ProductDO> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return page.convert(productConverter::toResp);
        }

        // 收集所有关联 ID
        Set<Long> brandIds = records.stream()
                                    .map(ProductDO::getBrandId)
                                    .collect(Collectors.toSet());
        Set<Long> imageIds = records.stream()
                                    .map(ProductDO::getImageId)
                                    .collect(Collectors.toSet());
        Set<Long> productIds = records.stream()
                                      .map(ProductDO::getId)
                                      .collect(Collectors.toSet());

        // 批量查询品牌
        Map<Long, BrandDO> brandMap = new HashMap<>();
        if (CollUtil.isNotEmpty(brandIds)) {
            List<BrandDO> brands = brandMapper.selectByIds(brandIds);
            brandMap = brands.stream().collect(Collectors.toMap(BrandDO::getId, Function.identity()));
        }

        // 批量查询主图
        Map<Long, ProductImageDO> imageMap = new HashMap<>();
        if (CollUtil.isNotEmpty(imageIds)) {
            List<ProductImageDO> images = productImageMapper.selectByIds(imageIds);
            imageMap = images.stream().collect(Collectors.toMap(ProductImageDO::getId, Function.identity()));
        }

        // 批量查询每个 SPU 的最低 SKU 价格
        Map<Long, BigDecimal> lowestPriceMap = productSkuMapper
                .selectLowestPrices(productIds)
                .stream()
                .collect(Collectors.toMap(ProductSkuPrice::productId, ProductSkuPrice::price));

        // 组装响应
        Map<Long, BrandDO> finalBrandMap = brandMap;
        Map<Long, ProductImageDO> finalImageMap = imageMap;
        return page.convert(
                product -> productConverter.toCompositeResp(
                        product,
                        finalBrandMap,
                        finalImageMap,
                        lowestPriceMap
                )
        );
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
        validateBrandAndCategory(req);
        validateSkuTopology(req);
        validatePrices(req);

        ProductDO productDO = saveProductWithImages(req, shopId);
        List<List<Long>> specValueIds = saveSpecs(productDO.getId(), req.getSpecGroups());
        saveSkus(productDO.getId(), req, specValueIds);
        return productDO.getId();
    }

    private void validateBrandAndCategory(ProductCreateReq req) {
        Asserts.notNull(brandMapper.selectById(req.getBrandId()), BizError.BRAND_NOT_EXIST);
        Asserts.notNull(
                categoryMapper.selectById(req.getCategoryId()),
                BizError.CATEGORY_NOT_EXIST_OR_NO_ENABLE
        );
    }

    private ProductDO saveProductWithImages(ProductCreateReq req, Long shopId) {
        ProductDO product = saveProduct(req, shopId);
        Long imageId = saveImages(req, product.getId());
        product.setImageId(imageId);
        productMapper.updateById(product);
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
     * 落商品图片，返回主图 ID。 <p>
     * 主图与排序均按下标推导：首张为主图，sortOrder 取下标。
     *
     * @param req 创建请求
     * @param productId SPU ID
     * @return 主图（首张图片）的 ID
     */
    private Long saveImages(ProductCreateReq req, Long productId) {
        List<ImageItem> images = req.getImages();
        List<ProductImageDO> imageDOList = new ArrayList<>(images.size());
        for (int i = 0; i < images.size(); i++) {
            ProductImageDO image = imageConverter.toDO(images.get(i), productId);
            image.setSortOrder(i);
            image.setIsMain(i == 0);
            imageDOList.add(image);
        }
        productImageMapper.insert(imageDOList);
        return imageDOList.get(0).getId();
    }

    /**
     * 落规格组与规格值，同时返回 [组下标][值下标] -> 规格值主键 的二维映射。 <p>
     * 这张映射是把请求里的 {@code specValues} 翻译成外键的唯一依据，方法返回后即丢弃，
     * 不落库。 规格组与规格值的 sortOrder 取各自的数组下标。 <p>
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
            group.setSortOrder(g);
            productSpecGroupMapper.insert(group);

            List<SpecValueItem> values = groupItem.getValues();
            List<Long> idsOfThisGroup = new ArrayList<>(values.size());
            for (int v = 0; v < values.size(); v++) {
                ProductSpecValueDO specValue = specValueConverter.toDO(values.get(v), group.getId());
                specValue.setValue(normalize(values.get(v).getValue()));
                specValue.setSortOrder(v);
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
            // specValues -> 规格值主键
            List<Long> realIds = new ArrayList<>(groups.size());
            for (int g = 0; g < groups.size(); g++) {
                String target = normalize(skuItem.getSpecValues().get(g));
                int v = indexOfValue(groups.get(g).getValues(), target);
                realIds.add(specValueIds.get(g).get(v));
            }

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
     * 获取商品详情。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public ProductDetailResp detail(Long id) {
        ProductDO product = Asserts.requireNonNull(productMapper.selectById(id));
        BrandDO brand = brandMapper.selectById(product.getBrandId());
        List<ImageResp> images = productImageService.listByProductId(id);
        List<SpecGroupResp> specGroups = productSpecService.getSpecGroups(id);
        List<ProductSkuDO> skus = productSkuMapper.selectByProductId(id);

        return productConverter.toDetailResp(product, brand, images, skus, specGroups);
    }

    /**
     * 校验规格组与 SKU 的拓扑结构。 <p>
     * 逐项校验：组名不重复、组内规格值不重复、每个 SKU 为每个规格组恰好指定一个值、 第 g 位的值确
     * 实存在于第 g 个规格组、SKU 之间规格组合不重复。 <p>
     * 按组隔离查找是这里的关键：{@code ["M", "L"]} 这种请求里，"M" 虽是合法规格值但
     * 属于尺码组， 第 0 位只在颜色组的候选里查，因此查不到而被拒绝。
     *
     * @param req 创建请求
     * @throws BizException 处理过程中可能抛出该异常
     */
    private void validateSkuTopology(ProductCreateReq req) {
        List<SpecGroupItem> groups = req.getSpecGroups();

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
        for (SkuItem sku : req.getSkus()) {
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
     * @param req 创建请求
     * @throws BizException 处理过程中可能抛出该异常
     */
    private void validatePrices(ProductCreateReq req) {
        for (SkuItem sku : req.getSkus()) {
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
