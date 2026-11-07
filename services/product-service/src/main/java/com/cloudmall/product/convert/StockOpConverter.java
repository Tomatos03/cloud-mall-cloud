package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.model.entity.StockOpLogDO;
import com.cloudmall.product.enums.StockOpSourceEnum;
import com.cloudmall.product.enums.StockOpTypeEnum;

/**
 * 库存流水转换器
 * <p>
 * 同名参数自动映射到 StockOpLogDO，避免 Service 层手写 builder。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface StockOpConverter {

    /**
     * 组装库存流水。
     *
     * @param orderNo 订单号
     * @param skuId SKU ID
     * @param productId 商品 ID
     * @param shopId 店铺 ID
     * @param opType 操作类型
     * @param quantity 数量
     * @param beforeStock 操作前库存
     * @param afterStock 操作后库存
     * @param source 来源
     * @param operatorId 操作人
     * @param reason 原因
     * @return 流水实体
     */
    @Mapping(target = "id", ignore = true)
    StockOpLogDO toLog(
        String orderNo,
        Long skuId,
        Long productId,
        Long shopId,
        StockOpTypeEnum opType,
        Integer quantity,
        Integer beforeStock,
        Integer afterStock,
        StockOpSourceEnum source,
        Long operatorId,
        String reason
    );
}
