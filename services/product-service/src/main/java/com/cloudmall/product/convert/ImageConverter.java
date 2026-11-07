package com.cloudmall.product.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.model.bo.ImageItem;
import com.cloudmall.product.api.model.resp.ImageResp;
import com.cloudmall.product.model.entity.ProductImageDO;

/**
 * 图片对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface ImageConverter {

    /**
     * 将图片请求模型转换为实体。 <p>
     * isMain 与 sort 由 Service 层按图片在列表中的下标赋值。
     *
     * @param item 图片请求模型
     * @param productId 所属商品 ID
     * @return 图片实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sort", ignore = true)
    @Mapping(target = "isMain", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "productId", source = "productId")
    ProductImageDO toDO(ImageItem item, Long productId);

    /**
     * 将图片实体转换为图片响应。
     *
     * @param image 图片实体
     * @return 图片响应
     */
    @Mapping(target = "imageId", source = "id")
    ImageResp toResp(ProductImageDO image);

    /**
     * 将图片实体列表转换为图片响应列表。
     *
     * @param images 图片实体列表
     * @return 图片响应列表
     */
    List<ImageResp> toRespList(List<ProductImageDO> images);
}
