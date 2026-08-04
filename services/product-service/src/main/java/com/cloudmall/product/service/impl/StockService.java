package com.cloudmall.product.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.request.StockConfirmReq;
import com.cloudmall.product.api.request.StockOpReq;
import com.cloudmall.product.api.request.StockReserveItem;
import com.cloudmall.product.convert.StockOpConverter;
import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.entity.ProductSkuDO;
import com.cloudmall.product.entity.StockOpLogDO;
import com.cloudmall.product.enums.StockOpSourceEnum;
import com.cloudmall.product.enums.StockOpTypeEnum;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.product.mapper.ProductSkuMapper;
import com.cloudmall.product.mapper.StockOpLogMapper;
import com.cloudmall.product.service.IStockService;

/**
 * 普通商品库存服务实现
 * <p>
 * 先写流水（唯一键挡并发）再改库存，同一事务保证原子性；
 * 库存不足抛异常回滚，重复请求由唯一键幂等跳过。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class StockService implements IStockService {

    private final ProductSkuMapper productSkuMapper;
    private final ProductMapper productMapper;
    private final StockOpLogMapper stockOpLogMapper;
    private final StockOpConverter stockOpConverter;

    /**
     * 预占库存。
     *
     * @param req 预占请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(StockOpReq req) {
        for (StockReserveItem item : req.getItems()) {
            applyOp(req, item, StockOpTypeEnum.RESERVE, StockOpSourceEnum.ORDER, true);
        }
    }

    /**
     * 支付确认（不改库存）。
     *
     * @param req 确认请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(StockConfirmReq req) {
        if (opExists(req.getOrderNo(), null, StockOpTypeEnum.CONFIRM)) {
            return;
        }
        List<StockOpLogDO> reserves = selectReserves(req.getOrderNo());
        if (CollUtil.isEmpty(reserves)) {
            return;
        }
        for (StockOpLogDO reserve : reserves) {
            insertLog(
                    req.getOrderNo(),
                    reserve.getSkuId(),
                    reserve.getProductId(),
                    reserve.getShopId(),
                    StockOpTypeEnum.CONFIRM,
                    reserve.getQuantity(),
                    null,
                    null,
                    StockOpSourceEnum.ORDER,
                    null,
                    "支付成功确认库存"
            );
        }
    }

    /**
     * 释放库存。
     *
     * @param req 释放请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(StockOpReq req) {
        for (StockReserveItem item : req.getItems()) {
            applyOp(req, item, StockOpTypeEnum.RELEASE, StockOpSourceEnum.ORDER, false);
        }
    }

    /**
     * 补偿释放。
     *
     * @param req 补偿请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void compensate(StockOpReq req) {
        for (StockReserveItem item : req.getItems()) {
            applyOp(req, item, StockOpTypeEnum.COMPENSATE, StockOpSourceEnum.COMPENSATE, false);
        }
    }

    /**
     * 执行一次库存操作：先查幂等，先写流水（唯一键挡并发）再改库存。
     *
     * @param req 请求
     * @param item 操作明细
     * @param opType 操作类型
     * @param source 来源
     * @param deduct true 表示扣减，false 表示回补
     */
    private void applyOp(
            StockOpReq req,
            StockReserveItem item,
            StockOpTypeEnum opType,
            StockOpSourceEnum source,
            boolean deduct
    ) {
        if (opExists(req.getOrderNo(), item.getSkuId(), opType)) {
            return;
        }
        ProductSkuDO sku = Asserts.requireNonNull(productSkuMapper.selectById(item.getSkuId()));
        int before = ObjectUtil.defaultIfNull(sku.getStock(), 0);
        StockOpLogDO log = insertLog(
                req.getOrderNo(),
                sku.getId(),
                sku.getProductId(),
                resolveShopId(sku.getProductId()),
                opType,
                item.getQuantity(),
                before,
                null,
                source,
                null,
                req.getReason()
        );
        if (log == null) {
            return;
        }
        applyStockChange(deduct, item);
        int after = ObjectUtil.defaultIfNull(loadStock(item.getSkuId()), 0);
        updateAfterStock(log.getId(), after);
    }

    /**
     * 按订单释放全部预占库存（取消/超时/补偿场景，幂等）。
     *
     * @param orderNo 订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseByOrder(String orderNo) {
        List<StockOpLogDO> reserves = selectReserves(orderNo);
        for (StockOpLogDO reserve : reserves) {
            StockReserveItem item = StockReserveItem.builder()
                                                    .skuId(reserve.getSkuId())
                                                    .quantity(reserve.getQuantity())
                                                    .build();
            applyOp(
                    StockOpReq.builder()
                              .orderNo(orderNo)
                              .items(List.of(item))
                              .reason("订单取消/超时释放")
                              .build(),
                    item,
                    StockOpTypeEnum.RELEASE,
                    StockOpSourceEnum.ORDER,
                    false
            );
        }
    }

    /**
     * 执行库存变更：扣减或回补，扣减失败（库存不足）抛异常。
     *
     * @param deduct true 表示扣减，false 表示回补
     * @param item 操作明细
     */
    private void applyStockChange(boolean deduct, StockReserveItem item) {
        int affected = deduct
                ? productSkuMapper.reserveStock(item.getSkuId(), item.getQuantity())
                : productSkuMapper.releaseStock(item.getSkuId(), item.getQuantity());
        if (deduct && affected == 0) {
            throw new BizException(BizError.STOCK_NOT_ENOUGH);
        }
    }

    /**
     * 写入库存流水，唯一键冲突视为已处理（幂等返回 null）。
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
     * @return 流水实体（已处理返回 null）
     */
    private StockOpLogDO insertLog(
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
    ) {
        StockOpLogDO log = stockOpConverter.toLog(
                orderNo,
                skuId,
                productId,
                shopId,
                opType,
                quantity,
                beforeStock,
                afterStock,
                source,
                operatorId,
                reason
        );
        try {
            stockOpLogMapper.insert(log);
        } catch (DuplicateKeyException ignored) {
            return null;
        }
        return log;
    }

    /**
     * 解析商品所属店铺 ID。
     *
     * @param productId 商品 ID
     * @return 店铺 ID（商品不存在为 null）
     */
    private Long resolveShopId(Long productId) {
        ProductDO product = productMapper.selectById(productId);
        return product == null ? null : product.getShopId();
    }

    /**
     * 更新流水操作后库存。
     *
     * @param logId 流水 ID
     * @param afterStock 操作后库存
     */
    private void updateAfterStock(Long logId, Integer afterStock) {
        stockOpLogMapper.update(
                Wrappers.<StockOpLogDO>lambdaUpdate()
                        .set(StockOpLogDO::getAfterStock, afterStock)
                        .eq(StockOpLogDO::getId, logId)
        );
    }

    /**
     * 查询订单预占流水。
     *
     * @param orderNo 订单号
     * @return 预占流水列表
     */
    private List<StockOpLogDO> selectReserves(String orderNo) {
        return stockOpLogMapper.selectList(
                Wrappers.<StockOpLogDO>lambdaQuery()
                        .eq(StockOpLogDO::getOrderNo, orderNo)
                        .eq(StockOpLogDO::getOpType, StockOpTypeEnum.RESERVE)
        );
    }

    /**
     * 判断订单号 + SKU + 操作类型是否已处理（流水幂等）。
     *
     * @param orderNo 订单号
     * @param skuId SKU ID（确认流水为 null）
     * @param opType 操作类型
     * @return true 表示已处理
     */
    private boolean opExists(String orderNo, Long skuId, StockOpTypeEnum opType) {
        Long count = stockOpLogMapper.selectCount(
                Wrappers.<StockOpLogDO>lambdaQuery()
                        .eq(StockOpLogDO::getOrderNo, orderNo)
                        .eq(skuId != null, StockOpLogDO::getSkuId, skuId)
                        .eq(StockOpLogDO::getOpType, opType)
        );
        return count != null && count > 0;
    }

    /**
     * 查询当前库存。
     *
     * @param skuId SKU ID
     * @return 当前库存
     */
    private Integer loadStock(Long skuId) {
        ProductSkuDO sku = productSkuMapper.selectById(skuId);
        return sku == null ? null : sku.getStock();
    }
}
