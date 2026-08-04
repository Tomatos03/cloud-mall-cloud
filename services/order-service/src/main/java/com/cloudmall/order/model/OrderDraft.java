package com.cloudmall.order.model;

import java.util.Map;

import com.cloudmall.order.entity.OrderDO;
import com.cloudmall.order.entity.OrderSubDO;

/**
 * 下单骨架：父订单与店铺子订单。
 * <p>
 * 在 Saga 预创建阶段持有，待锁券与库存预占完成后由 finalizeOrder 填充金额与明细。
 *
 * @param order 父订单
 * @param subsByShop shopId → 子订单
 * @author : Tomatos
 */
public record OrderDraft(OrderDO order, Map<Long, OrderSubDO> subsByShop) {
}
