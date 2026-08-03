package com.cloudmall.product.convert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.request.ProductCreateReq;
import com.cloudmall.product.api.response.ImageResp;
import com.cloudmall.product.api.response.ProductDetailResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.api.response.SpecGroupResp;
import com.cloudmall.product.entity.BrandDO;
import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.entity.ProductImageDO;
import com.cloudmall.product.entity.ProductSkuDO;

/**
 * 商品相关对象转换器。
 * <p>
 * SPU 查询后的品牌、图片、SKU 等信息在 Service 层通过多次查询组合，
 * 转换器负责将各层数据组装为统一的 API 响应。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring", uses = {BrandConverter.class, SkuConverter.class})
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
     * spuCode 由服务端在 insert 后基于自增 ID 生成，imageId 在图片创建后设置，
     * 审计字段由 MyBatis-Plus 自动填充。
     *
     * @param req 创建请求
     * @return SPU 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "spuCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(
            target = "status",
            expression = "java(com.cloudmall.mybatisplus.enums.StatusEnum.ENABLED)"
    )
    ProductDO toDO(ProductCreateReq req);

    /**
     * 将 SPU DO 及其关联数据组装为列表页响应。
     *
     * @param product SPU 实体
     * @param brandMap 品牌 ID -> 品牌 DO 映射
     * @param imageMap 图片 ID -> 图片 DO 映射
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
        if (imageMap != null && product.getImageId() != null) {
            ProductImageDO img = imageMap.get(product.getImageId());
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
     * brand 与 skus 分别委托 {@link BrandConverter}、
     * {@link SkuConverter} 转换， 由 MapStruct 在生成实现中自动注入调用。
     *
     * @param product SPU 实体
     * @param brand 品牌实体
     * @param images 图片列表
     * @param skus SKU 实体列表
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
    @Mapping(target = "categoryId", source = "product.categoryId")
    @Mapping(target = "description", source = "product.description")
    ProductDetailResp toDetailResp(
            ProductDO product,
            BrandDO brand,
            List<ImageResp> images,
            List<ProductSkuDO> skus,
            List<SpecGroupResp> specGroups
    );

}
