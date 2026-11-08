package com.cloudmall.mockpayment.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mockpayment.enums.NotifyStatusEnum;
import com.cloudmall.mockpayment.model.entity.MockPaymentNotifyDO;

/**
 * 模拟支付回调通知 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface MockPaymentNotifyMapper extends BaseMapper<MockPaymentNotifyDO> {

    /**
     * 查询待重试的通知（按主键升序，限制数量）。
     *
     * @param now 当前时间
     * @param maxRetry 最大重试次数
     * @param limit 单批扫描数量
     * @return 待重试通知
     */
    default List<MockPaymentNotifyDO> selectRetryCandidates(LocalDateTime now, int maxRetry, int limit) {
        Page<MockPaymentNotifyDO> page = new Page<>(1, limit, false);
        return selectPage(page, Wrappers.<MockPaymentNotifyDO>lambdaQuery()
                                        .in(
                                                MockPaymentNotifyDO::getStatus,
                                                NotifyStatusEnum.PENDING.getCode(),
                                                NotifyStatusEnum.FAILED.getCode()
                                        )
                                        .lt(MockPaymentNotifyDO::getRetryCount, maxRetry)
                                        .le(MockPaymentNotifyDO::getNextRetryTime, now)
                                        .orderByAsc(MockPaymentNotifyDO::getId))
                .getRecords();
    }

    /**
     * 查询订单下的全部通知记录（按主键倒序）。
     *
     * @param outTradeNo 商户订单号
     * @return 通知记录
     */
    default List<MockPaymentNotifyDO> selectByOutTradeNo(String outTradeNo) {
        return selectList(
                Wrappers.<MockPaymentNotifyDO>lambdaQuery()
                        .eq(MockPaymentNotifyDO::getOutTradeNo, outTradeNo)
                        .orderByDesc(MockPaymentNotifyDO::getId)
        );
    }

    /**
     * 保存最近一次回调签名值。
     *
     * @param id 通知记录 ID
     * @param signature 签名值
     * @return 影响行数
     */
    default int updateSignature(Long id, String signature) {
        return update(
                Wrappers.<MockPaymentNotifyDO>lambdaUpdate()
                        .set(MockPaymentNotifyDO::getSignature, signature)
                        .eq(MockPaymentNotifyDO::getId, id)
        );
    }

    /**
     * 标记投递成功。
     *
     * @param id 通知记录 ID
     * @param responseBody 应答报文
     * @return 影响行数
     */
    default int markSuccess(Long id, String responseBody) {
        return update(
                Wrappers.<MockPaymentNotifyDO>lambdaUpdate()
                        .set(MockPaymentNotifyDO::getStatus, NotifyStatusEnum.SUCCESS.getCode())
                        .set(MockPaymentNotifyDO::getResponseBody, responseBody)
                        .set(MockPaymentNotifyDO::getLastError, null)
                        .eq(MockPaymentNotifyDO::getId, id)
        );
    }

    /**
     * 标记投递失败并记录下次重试时间。
     *
     * @param id 通知记录 ID
     * @param retryCount 已重试次数
     * @param nextRetryTime 下次重试时间
     * @param responseBody 应答报文
     * @param lastError 失败原因
     * @return 影响行数
     */
    default int markFailed(
        Long id,
        int retryCount,
        LocalDateTime nextRetryTime,
        String responseBody,
        String lastError
    ) {
        return update(
                Wrappers.<MockPaymentNotifyDO>lambdaUpdate()
                        .set(MockPaymentNotifyDO::getStatus, NotifyStatusEnum.FAILED.getCode())
                        .set(MockPaymentNotifyDO::getRetryCount, retryCount)
                        .set(MockPaymentNotifyDO::getNextRetryTime, nextRetryTime)
                        .set(MockPaymentNotifyDO::getResponseBody, responseBody)
                        .set(MockPaymentNotifyDO::getLastError, lastError)
                        .eq(MockPaymentNotifyDO::getId, id)
        );
    }

    /**
     * 重置通知记录以便手动重发。
     *
     * @param id 通知记录 ID
     * @param now 当前时间
     * @return 影响行数
     */
    default int resetForResend(Long id, LocalDateTime now) {
        return update(
                Wrappers.<MockPaymentNotifyDO>lambdaUpdate()
                        .set(MockPaymentNotifyDO::getStatus, NotifyStatusEnum.PENDING.getCode())
                        .set(MockPaymentNotifyDO::getRetryCount, 0)
                        .set(MockPaymentNotifyDO::getNextRetryTime, now)
                        .set(MockPaymentNotifyDO::getLastError, null)
                        .eq(MockPaymentNotifyDO::getId, id)
        );
    }
}
