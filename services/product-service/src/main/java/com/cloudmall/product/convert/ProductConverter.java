package com.cloudmall.product.convert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.model.req.ProductCreateReq;
import com.cloudmall.product.api.model.resp.CommentSummaryResp;
import com.cloudmall.product.api.model.resp.ImageResp;
import com.cloudmall.product.api.model.resp.ProductDetailResp;
import com.cloudmall.product.api.model.resp.ProductResp;
import com.cloudmall.product.api.model.resp.ProductSku;
import com.cloudmall.product.api.model.resp.SpecGroupResp;
import com.cloudmall.product.model.entity.BrandDO;
import com.cloudmall.product.model.entity.CategoryDO;
import com.cloudmall.product.model.entity.ProductDO;
import com.cloudmall.product.model.entity.ProductImageDO;
import com.cloudmall.user.api.model.resp.ShopInfoResp;

/**
 * 商品相关对象转换器。
 * <p>
 * SPU 查询后的品牌、图片、SKU 等信息在 Service 层通过多次查询组合，
 * 转换器负责将各层数据组装为统一的 API 响应。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring", uses = BrandConverter.class)
public interface ProductConverter {

    /**
     * 将 SPU DO 转换为列表页响应的基础信息。 <p>
     * Service 层需额外填充 brand、images、price、stock 等跨表字段。
     *
     * @param product SPU 实体
     * @return 商品列表响应
     */
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    ProductResp toResp(ProductDO product);

    /**
     * 将创建请求转换为 SPU 实体。 <p>
     * spuCode 由服务端在 insert 后基于自增 ID 生成， 审计字段由
     * MyBatis-Plus 自动填充。
     *
     * @param req 创建请求
     * @return SPU 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "spuCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "status", expression = "java(com.cloudmall.mybatisplus.enums.StatusEnum.ENABLED)")
    ProductDO toDO(ProductCreateReq req);

    /**
     * 将 SPU DO 及其关联数据组装为列表页响应。
     *
     * @param product SPU 实体
     * @param brandMap 品牌 ID -> 品牌 DO 映射
     * @param imageMap 商品 ID -> 主图 DO 映射
     * @param lowestPriceMap SPU ID -> 最低价映射
     * @return 组装后的商品列表响应
     */
    default ProductResp toCompositeResp(
        ProductDO product,
        Map<Long, BrandDO> brandMap,
        Map<Long, ProductImageDO> imageMap,
        Map<Long, BigDecimal> lowestPriceMap
    ) {
        ProductResp resp = toResp(product);
        if (brandMap != null && product.getBrandId() != null) {
            BrandDO brand = brandMap.get(product.getBrandId());
            if (brand != null) {
                resp.setBrand(brand.getName());
            }
        }
        if (imageMap != null) {
            ProductImageDO img = imageMap.get(product.getId());
            if (img != null) {
                resp.setImage(img.getUrl());
            }
        }
        if (lowestPriceMap != null) {
            resp.setPrice(lowestPriceMap.get(product.getId()));
        }
        return resp;
    }

    /**
     * 将 SPU DO 及其完整关联数据组装为详情页响应。 <p>
     * brand 与 category 委托 {@link BrandConverter} 转换，skus
     * 由 Service 层预先转换为 {@link ProductSku} 后传入。
     *
     * @param product SPU 实体
     * @param brand 品牌实体
     * @param category 分类实体
     * @param images 图片列表
     * @param skus SKU 响应列表
     * @param specGroups 规格分组
     * @return 商品详情响应
     */
    @Mapping(target = "skus", source = "skus")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "specGroups", source = "specGroups")
    @Mapping(target = "spuCode", source = "product.spuCode")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryId", source = "product.categoryId")
    @Mapping(target = "description", source = "product.description")
    ProductDetailResp toDetailResp(
        ProductDO product,
        BrandDO brand,
        CategoryDO category,
        List<ImageResp> images,
        List<ProductSku> skus,
        List<SpecGroupResp> specGroups
    );

    /**
     * 填充商品详情的店铺信息与评论摘要聚合块。
     *
     * @param resp 商品详情响应
     * @param shop 店铺信息
     * @param commentSummary 评论摘要
     * @return 聚合后的商品详情响应
     */
    default ProductDetailResp applyAggregations(
        ProductDetailResp resp,
        ShopInfoResp shop,
        CommentSummaryResp commentSummary
    ) {
        resp.setShop(shop);
        resp.setCommentSummary(commentSummary);
        return resp;
    }
}
