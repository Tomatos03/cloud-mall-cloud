package com.cloudmall.product.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.model.SpecGroupItem;
import com.cloudmall.product.api.response.SpecGroupResp;
import com.cloudmall.product.entity.ProductSpecGroupDO;
import com.cloudmall.product.entity.ProductSpecValueDO;

/**
 * 规格组对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring", uses = SpecValueConverter.class)
public interface SpecGroupConverter {

    /**
     * 将规格组请求模型转换为实体。
     *
     * @param item 规格组请求模型
     * @param productId 所属商品 ID
     * @return 规格组实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "productId", source = "productId")
    ProductSpecGroupDO toDO(SpecGroupItem item, Long productId);

    /**
     * 将规格组实体及其规格值列表组装为规格组响应。 <p>
     * 规格值列表委托 {@link SpecValueConverter} 转换，由 MapStruct
     * 自动注入调用。
     *
     * @param group 规格组实体
     * @param values 组内规格值实体列表
     * @return 规格组响应
     */
    @Mapping(target = "values", source = "values")
    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "groupId", source = "group.id")
    SpecGroupResp toResp(ProductSpecGroupDO group, List<ProductSpecValueDO> values);
}
