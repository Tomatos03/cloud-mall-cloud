package com.cloudmall.product.convert;

import java.util.List;

import org.mapstruct.Mapper;

import com.cloudmall.product.api.model.resp.ProductQaResp;
import com.cloudmall.product.model.entity.ProductQaDO;

/**
 * 商品问答转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface ProductQaConverter {

    /**
     * 问答实体转为响应。
     *
     * @param qa 问答实体
     * @return 问答响应
     */
    ProductQaResp toResp(ProductQaDO qa);

    /**
     * 问答实体列表转为响应列表。
     *
     * @param qas 问答实体列表
     * @return 问答响应列表
     */
    List<ProductQaResp> toRespList(List<ProductQaDO> qas);
}
