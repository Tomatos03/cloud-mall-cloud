package com.cloudmall.product.mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.product.entity.ProductSkuDO;
import com.cloudmall.product.model.ProductSkuPrice;

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
            """
            UPDATE cm_product_sku
            SET stock = stock - #{quantity}
            WHERE id = #{skuId} AND stock >= #{quantity} AND deleted = 0
            """
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
    @Update(
            """
            UPDATE cm_product_sku
            SET stock = stock + #{quantity}
            WHERE id = #{skuId} AND deleted = 0
            """
    )
    int releaseStock(
            @Param("skuId")
            Long skuId,
            @Param("quantity")
            Integer quantity
    );

    /**
     * 查询指定 SPU 下所有 SKU 的最低价格。
     *
     * @param productId SPU ID
     * @return 最低价格，无 SKU 时返回 null
     */
    @Select("SELECT MIN(price) FROM cm_product_sku WHERE product_id = #{productId} AND deleted = 0")
    BigDecimal selectLowestPrice(
            @Param("productId")
            Long productId
    );

    /**
     * 批量查询多个 SPU 下每个 SPU 的最低 SKU 价格。
     *
     * @param productIds SPU ID 集合
     * @return 每个 SPU 的最低价格行
     */
    @Select(
            """
            <script>
            SELECT product_id AS productId, MIN(price) AS price
            FROM cm_product_sku
            WHERE deleted = 0 AND product_id IN
            <foreach collection="productIds" item="productId" open="(" separator="," close=")">
                #{productId}
            </foreach>
            GROUP BY product_id
            </script>
            """
    )
    List<ProductSkuPrice> selectLowestPrices(
            @Param("productIds")
            Collection<Long> productIds
    );

    /**
     * 查询指定 SPU 下的全部 SKU。
     *
     * @param productId SPU ID
     * @return SKU 列表
     */
    @Select("SELECT * FROM cm_product_sku WHERE product_id = #{productId} AND deleted = 0")
    List<ProductSkuDO> selectByProductId(
            @Param("productId")
            Long productId
    );
}
