package com.cloudmall.product.service;

import java.util.List;

import com.cloudmall.product.api.response.SkuResp;

/**
 * 商品 SKU 服务接口。
 *
 * @author : Tomatos
 */
public interface ISkuService {

    /**
     * 扣减 SKU 库存。 <p>
     * quantity 为正数，条件更新（stock >= quantity）防止超卖，库存不足抛
     * {@code STOCK_NOT_ENOUGH}。
     *
     * @param skuId SKU ID
     * @param quantity 扣减数量（正数）
     * @return 操作是否成功
     */
    Boolean deductStock(Long skuId, Integer quantity);

    /**
     * 回滚 SKU 库存（增加库存）。 <p>
     * quantity 为正数，无条件加回。
     *
     * @param skuId SKU ID
     * @param quantity 回滚数量（正数）
     * @return 操作是否成功
     */
    Boolean releaseStock(Long skuId, Integer quantity);

    /**
     * 批量查询 SKU 信息（含实时价格、库存、规格描述、所属店铺）。 <p>
     * 仅返回上架商品的 SKU；供购物车、订单等场景一次取齐，避免逐条调用；skuIds 为空时返回空列表
     * 。
     *
     * @param skuIds SKU ID 列表
     * @return 上架商品的 SKU 信息列表（顺序不保证）
     */
    List<SkuResp> listSkusByIds(List<Long> skuIds);

    /**
     * 按已选规格值匹配唯一 SKU。 <p>
     * 用于商品详情页规格选择联动：当所有规格组均有选值时恰好命中一个 SKU， 返回该 SKU 的完整信息
     * （含实时价格、库存、规格描述）； 未选中全部规格组或组合不成立时返回 null。
     *
     * @param productId 商品 ID
     * @param specValueIds 已选规格值 ID 列表
     * @return 唯一命中的 SKU 信息，未命中时为 null
     */
    SkuResp matchSkuBySpec(Long productId, List<Long> specValueIds);
}
