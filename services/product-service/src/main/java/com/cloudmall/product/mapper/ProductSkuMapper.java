package com.cloudmall.product.mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.model.bo.ProductSkuPrice;
import com.cloudmall.product.model.entity.ProductSkuDO;

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
    default int reserveStock(Long skuId, Integer quantity) {
        final String stockSql = "stock = stock - " + quantity;
        return update(
                Wrappers.<ProductSkuDO>lambdaUpdate()
                        .setSql(stockSql)
                        .eq(ProductSkuDO::getId, skuId)
                        .ge(ProductSkuDO::getStock, quantity)
        );
    }

    /**
     * 回补库存（释放/补偿）。
     *
     * @param skuId SKU ID
     * @param quantity 回补数量
     * @return 影响行数
     */
    default int releaseStock(Long skuId, Integer quantity) {
        final String stockSql = "stock = stock + " + quantity;
        return update(Wrappers.<ProductSkuDO>lambdaUpdate()
                              .setSql(stockSql)
                              .eq(ProductSkuDO::getId, skuId));
    }

    /**
     * 查询指定 SPU 下所有 SKU 的最低价格。
     *
     * @param productId SPU ID
     * @return 最低价格，无 SKU 时返回 null
     */
    default BigDecimal selectLowestPrice(Long productId) {
        List<Object> objs = selectObjs(
                Wrappers.<ProductSkuDO>query()
                        .select("MIN(price)")
                        .eq("product_id", productId)
        );
        if (objs.isEmpty() || objs.get(0) == null) {
            return null;
        }
        return new BigDecimal(objs.get(0).toString());
    }

    /**
     * 批量查询多个 SPU 下每个 SPU 的最低 SKU 价格。
     *
     * @param productIds SPU ID 集合
     * @return 每个 SPU 的最低价格行
     */
    default List<ProductSkuPrice> selectLowestPrices(Collection<Long> productIds) {
        List<Map<String, Object>> rows = selectMaps(
                Wrappers.<ProductSkuDO>query()
                        .select("product_id AS productId", "MIN(price) AS price")
                        .in("product_id", productIds)
                        .groupBy("product_id")
        );
        return rows
                .stream()
                .map(
                        row -> new ProductSkuPrice(
                                ((Number) row.get("productId")).longValue(),
                                new BigDecimal(row.get("price").toString())
                        )
                )
                .toList();
    }

    /**
     * 查询指定 SPU 下的全部 SKU。
     *
     * @param productId SPU ID
     * @return SKU 列表
     */
    default List<ProductSkuDO> selectByProductId(Long productId) {
        return selectList(Wrappers.<ProductSkuDO>lambdaQuery().eq(ProductSkuDO::getProductId, productId));
    }

    /**
     * 清空多个 SKU 的规格组合指纹（软删除前释放唯一槽位）。
     *
     * @param ids SKU ID 集合
     * @return 影响行数
     */
    default int clearSpecHashByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return update(
                Wrappers.<ProductSkuDO>lambdaUpdate()
                        .set(ProductSkuDO::getSpecHash, null)
                        .in(ProductSkuDO::getId, ids)
        );
    }
}
