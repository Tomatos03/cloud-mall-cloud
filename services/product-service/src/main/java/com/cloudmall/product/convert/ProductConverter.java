package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.entity.ProductDO;

@Mapper(componentModel = "spring")
public interface ProductConverter {

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "images", ignore = true)
    ProductResp toResp(ProductDO product);
}
