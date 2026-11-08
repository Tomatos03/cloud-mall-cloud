package com.cloudmall.mockpayment.mapper;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mockpayment.model.entity.MockPaymentOrderDO;
import com.cloudmall.payment.api.enums.TradeStateEnum;

/**
 * 模拟支付单 Mapper。
 * <p>
 * 状态变更全部使用条件更新，保证同一商户订单号在并发支付、关单时只有一次成功迁移。
 *
 * @author : Tomatos
 */
@Mapper
public interface MockPaymentOrderMapper extends BaseMapper<MockPaymentOrderDO> {

    /**
     * 按商户订单号查询支付单。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单，不存在时返回 null
     */
    default MockPaymentOrderDO selectByOutTradeNo(String outTradeNo) {
        return selectOne(
                Wrappers.<MockPaymentOrderDO>lambdaQuery().eq(MockPaymentOrderDO::getOutTradeNo, outTradeNo)
        );
    }

    /**
     * 按模拟微信支付订单号查询支付单。
     *
     * @param transactionId 模拟微信支付订单号
     * @return 支付单，不存在时返回 null
     */
    default MockPaymentOrderDO selectByTransactionId(String transactionId) {
        return selectOne(
                Wrappers.<MockPaymentOrderDO>lambdaQuery()
                        .eq(MockPaymentOrderDO::getTransactionId, transactionId)
        );
    }

    /**
     * 分页查询模拟支付单（按创建时间倒序）。
     *
     * @param page 分页参数
     * @param tradeState 交易状态，可为空
     * @return 支付单分页
     */
    default IPage<MockPaymentOrderDO> selectOrderPage(Page<MockPaymentOrderDO> page, String tradeState) {
        return selectPage(
                page,
                Wrappers.<MockPaymentOrderDO>lambdaQuery()
                        .eq(tradeState != null, MockPaymentOrderDO::getTradeState, tradeState)
                        .orderByDesc(MockPaymentOrderDO::getId)
        );
    }

    /**
     * 未支付 → 支付成功（条件更新防并发）。
     *
     * @param outTradeNo 商户订单号
     * @param transactionId 模拟微信支付订单号
     * @param successTime 支付完成时间
     * @param tradeState 目标状态
     * @param tradeStateDesc 状态描述
     * @return 影响行数
     */
    default int markPaid(
        String outTradeNo,
        String transactionId,
        LocalDateTime successTime,
        String tradeState,
        String tradeStateDesc
    ) {
        return update(
                Wrappers.<MockPaymentOrderDO>lambdaUpdate()
                        .set(MockPaymentOrderDO::getTransactionId, transactionId)
                        .set(MockPaymentOrderDO::getTradeState, tradeState)
                        .set(MockPaymentOrderDO::getTradeStateDesc, tradeStateDesc)
                        .set(MockPaymentOrderDO::getSuccessTime, successTime)
                        .eq(MockPaymentOrderDO::getOutTradeNo, outTradeNo)
                        .eq(MockPaymentOrderDO::getTradeState, TradeStateEnum.NOTPAY.getCode())
        );
    }

    /**
     * 未支付 → 已关闭（条件更新防并发）。
     *
     * @param outTradeNo 商户订单号
     * @param tradeState 目标状态
     * @param tradeStateDesc 状态描述
     * @return 影响行数
     */
    default int markClosed(String outTradeNo, String tradeState, String tradeStateDesc) {
        return update(
                Wrappers.<MockPaymentOrderDO>lambdaUpdate()
                        .set(MockPaymentOrderDO::getTradeState, tradeState)
                        .set(MockPaymentOrderDO::getTradeStateDesc, tradeStateDesc)
                        .eq(MockPaymentOrderDO::getOutTradeNo, outTradeNo)
                        .eq(MockPaymentOrderDO::getTradeState, TradeStateEnum.NOTPAY.getCode())
        );
    }
}
