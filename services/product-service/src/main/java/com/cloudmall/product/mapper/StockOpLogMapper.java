package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.StockOpLogDO;
import com.cloudmall.product.enums.StockOpTypeEnum;

/**
 * 库存操作流水 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface StockOpLogMapper extends BaseMapper<StockOpLogDO> {

    /**
     * 回填流水操作后库存。
     *
     * @param logId 流水 ID
     * @param afterStock 操作后库存
     * @return 影响行数
     */
    default int updateAfterStock(Long logId, Integer afterStock) {
        return update(
                Wrappers.<StockOpLogDO>lambdaUpdate()
                        .set(StockOpLogDO::getAfterStock, afterStock)
                        .eq(StockOpLogDO::getId, logId)
        );
    }

    /**
     * 查询订单的预占流水。
     *
     * @param orderNo 订单号
     * @return 预占流水列表
     */
    default List<StockOpLogDO> selectReservesByOrderNo(String orderNo) {
        return selectList(
                Wrappers.<StockOpLogDO>lambdaQuery()
                        .eq(StockOpLogDO::getOrderNo, orderNo)
                        .eq(StockOpLogDO::getOpType, StockOpTypeEnum.RESERVE)
        );
    }

    /**
     * 统计订单号 + SKU + 操作类型的流水数量（用于幂等判断）。
     *
     * @param orderNo 订单号
     * @param skuId SKU ID（可空，空则忽略该条件）
     * @param opType 操作类型
     * @return 流水数量
     */
    default Long countByOrderNoAndOpType(String orderNo, Long skuId, StockOpTypeEnum opType) {
        return selectCount(
                Wrappers.<StockOpLogDO>lambdaQuery()
                        .eq(StockOpLogDO::getOrderNo, orderNo)
                        .eq(skuId != null, StockOpLogDO::getSkuId, skuId)
                        .eq(StockOpLogDO::getOpType, opType)
        );
    }
}
