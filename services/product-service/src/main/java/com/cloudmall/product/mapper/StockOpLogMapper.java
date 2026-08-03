package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.StockOpLogDO;

/**
 * 库存操作流水 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface StockOpLogMapper extends BaseMapper<StockOpLogDO> {
}
