package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.product.entity.ProductSkuDO;

/**
 * 商品 SKU Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSkuDO> {

    /**
     * 条件扣减库存：库存充足才扣减，防止扣成负数。
     *
     * @param skuId SKU ID
     * @param quantity 扣减数量
     * @return 影响行数，0 表示库存不足或 SKU 不存在
     */
    @Update(
            "UPDATE cm_product_sku SET stock = stock - #{quantity} " + "WHERE id = #{skuId} AND stock >= #{quantity} AND deleted = 0"
    )
    int reserveStock(
            @Param("skuId")
            Long skuId,
            @Param("quantity")
            Integer quantity
    );

    /**
     * 回补库存（释放/补偿）。
     *
     * @param skuId SKU ID
     * @param quantity 回补数量
     * @return 影响行数
     */
    @Update("UPDATE cm_product_sku SET stock = stock + #{quantity} " + "WHERE id = #{skuId} AND deleted = 0")
    int releaseStock(
            @Param("skuId")
            Long skuId,
            @Param("quantity")
            Integer quantity
    );
}
