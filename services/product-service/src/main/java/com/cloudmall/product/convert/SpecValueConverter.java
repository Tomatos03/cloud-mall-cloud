package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.model.SpecValueItem;
import com.cloudmall.product.api.response.SpecValueResp;
import com.cloudmall.product.entity.ProductSpecValueDO;

/**
 * 规格值对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface SpecValueConverter {

    /**
     * 将规格值请求模型转换为实体。
     *
     * @param item 规格值请求模型
     * @param groupId 所属规格组 ID
     * @return 规格值实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sort", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "groupId", source = "groupId")
    ProductSpecValueDO toDO(SpecValueItem item, Long groupId);

    /**
     * 将规格值实体转换为规格值响应。
     *
     * @param value 规格值实体
     * @return 规格值响应
     */
    @Mapping(target = "valueId", source = "id")
    SpecValueResp toResp(ProductSpecValueDO value);
}
