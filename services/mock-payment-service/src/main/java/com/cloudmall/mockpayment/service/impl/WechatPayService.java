package com.cloudmall.mockpayment.service.impl;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.mockpayment.convert.MockPaymentConverter;
import com.cloudmall.mockpayment.enums.TradeTypeEnum;
import com.cloudmall.mockpayment.mapper.MockPaymentOrderMapper;
import com.cloudmall.mockpayment.model.entity.MockPaymentOrderDO;
import com.cloudmall.mockpayment.model.req.CreatePayReq;
import com.cloudmall.mockpayment.model.resp.MockPaymentDetailResp;
import com.cloudmall.mockpayment.model.resp.TransactionQueryResp;
import com.cloudmall.mockpayment.properties.MockPaymentProperties;
import com.cloudmall.mockpayment.service.IWechatPayService;
import com.cloudmall.payment.api.constant.WechatPayConstants;
import com.cloudmall.payment.api.enums.TradeStateEnum;
import com.cloudmall.payment.api.model.req.MockPaymentCreateReq;
import com.cloudmall.payment.api.model.resp.MockPaymentCreateResp;
import com.cloudmall.payment.api.model.resp.MockPaymentQueryResp;

/**
 * 模拟微信支付服务实现。
 * <p>
 * 以微信支付 API v3 的交易模型组织支付单，金额使用分；
 * 下单、查询、关单、支付成功均使用条件更新保证幂等与并发安全。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class WechatPayService implements IWechatPayService {

    private final MockPaymentOrderMapper orderMapper;
    private final MockPaymentConverter converter;
    private final MockPaymentProperties properties;

    /**
     * 微信支付协议下单。
     *
     * @param req 下单请求
     * @param tradeType 交易类型
     * @return 预支付信息
     */
    @Override
    public MockPaymentCreateResp create(CreatePayReq req, TradeTypeEnum tradeType) {
        validateMerchant(req.getAppid(), req.getMchid());
        String payerOpenid = req.getPayer() == null ? null : req.getPayer().getOpenid();
        if (tradeType == TradeTypeEnum.JSAPI) {
            Asserts.notBlank(payerOpenid, BizErrorEnum.INVALID_PARAM);
        }
        return createOrder(
                req.getAppid(),
                req.getMchid(),
                req.getDescription(),
                req.getOutTradeNo(),
                req.getNotifyUrl(),
                req.getAttach(),
                req.getAmount().getTotal(),
                StrUtil.blankToDefault(req.getAmount().getCurrency(), WechatPayConstants.CURRENCY_CNY),
                payerOpenid,
                tradeType,
                parseExpire(req.getTimeExpire())
        );
    }

    /**
     * 内部接口下单。
     *
     * @param req 下单请求
     * @return 预支付信息
     */
    @Override
    public MockPaymentCreateResp createInternal(MockPaymentCreateReq req) {
        TradeTypeEnum tradeType = StrUtil.isBlank(req.getTradeType())
                ? TradeTypeEnum.NATIVE
                : TradeTypeEnum.fromCode(req.getTradeType());
        return createOrder(
                properties.getAppid(),
                properties.getMchid(),
                req.getDescription(),
                req.getOutTradeNo(),
                req.getNotifyUrl(),
                req.getAttach(),
                req.getAmountTotal(),
                WechatPayConstants.CURRENCY_CNY,
                req.getPayerOpenid(),
                tradeType,
                req.getExpireTime()
        );
    }

    /**
     * 按商户订单号查询订单。
     *
     * @param outTradeNo 商户订单号
     * @param mchid 商户号
     * @return 微信支付查询响应
     */
    @Override
    public TransactionQueryResp queryByOutTradeNo(String outTradeNo, String mchid) {
        validateMchid(mchid);
        MockPaymentOrderDO order = requireOrder(outTradeNo);
        refreshIfExpired(order);
        return converter.toQueryResp(order);
    }

    /**
     * 按模拟微信支付订单号查询订单。
     *
     * @param transactionId 模拟微信支付订单号
     * @param mchid 商户号
     * @return 微信支付查询响应
     */
    @Override
    public TransactionQueryResp queryByTransactionId(String transactionId, String mchid) {
        validateMchid(mchid);
        MockPaymentOrderDO order = Asserts.requireNonNull(
                orderMapper.selectByTransactionId(transactionId),
                BizErrorEnum.MOCK_PAYMENT_ORDER_NOT_EXIST
        );
        refreshIfExpired(order);
        return converter.toQueryResp(order);
    }

    /**
     * 内部接口查询订单。
     *
     * @param outTradeNo 商户订单号
     * @return 内部查询响应
     */
    @Override
    public MockPaymentQueryResp queryInternal(String outTradeNo) {
        MockPaymentOrderDO order = requireOrder(outTradeNo);
        refreshIfExpired(order);
        return converter.toQueryRespForInternal(order, publicBaseUrl());
    }

    /**
     * 微信支付协议关闭订单。
     *
     * @param outTradeNo 商户订单号
     * @param mchid 商户号
     */
    @Override
    public void close(String outTradeNo, String mchid) {
        validateMchid(mchid);
        doClose(outTradeNo);
    }

    /**
     * 内部接口关闭订单。
     *
     * @param outTradeNo 商户订单号
     */
    @Override
    public void closeInternal(String outTradeNo) {
        doClose(outTradeNo);
    }

    /**
     * 模拟支付成功。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    @Override
    public MockPaymentDetailResp markPaid(String outTradeNo) {
        MockPaymentOrderDO order = requireOrder(outTradeNo);
        if (TradeStateEnum.SUCCESS.getCode().equals(order.getTradeState())) {
            return converter.toDetailResp(order, publicBaseUrl());
        }
        Asserts.isFalse(
                TradeStateEnum.CLOSED.getCode().equals(order.getTradeState()),
                BizErrorEnum.MOCK_PAYMENT_ORDER_CLOSED
        );
        refreshIfExpired(order);
        Asserts.isFalse(
                TradeStateEnum.CLOSED.getCode().equals(order.getTradeState()),
                BizErrorEnum.MOCK_PAYMENT_ORDER_CLOSED
        );
        Asserts.isTrue(
                TradeStateEnum.NOTPAY.getCode().equals(order.getTradeState()),
                BizErrorEnum.INVALID_ORDER_STATUS
        );
        String transactionId = StrUtil.blankToDefault(order.getTransactionId(), generateTransactionId());
        LocalDateTime successTime = LocalDateTime.now().withNano(0);
        int updated = orderMapper.markPaid(
                outTradeNo,
                transactionId,
                successTime,
                TradeStateEnum.SUCCESS.getCode(),
                TradeStateEnum.SUCCESS.getDesc()
        );
        if (updated == 0) {
            MockPaymentOrderDO latest = requireOrder(outTradeNo);
            Asserts.isTrue(
                    TradeStateEnum.SUCCESS.getCode().equals(latest.getTradeState()),
                    BizErrorEnum.INVALID_ORDER_STATUS
            );
            return converter.toDetailResp(latest, publicBaseUrl());
        }
        order.setTransactionId(transactionId);
        order.setTradeState(TradeStateEnum.SUCCESS.getCode());
        order.setTradeStateDesc(TradeStateEnum.SUCCESS.getDesc());
        order.setSuccessTime(successTime);
        return converter.toDetailResp(order, publicBaseUrl());
    }

    /**
     * 查询支付单详情。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单详情
     */
    @Override
    public MockPaymentDetailResp detail(String outTradeNo) {
        MockPaymentOrderDO order = requireOrder(outTradeNo);
        refreshIfExpired(order);
        return converter.toDetailResp(order, publicBaseUrl());
    }

    /**
     * 分页查询模拟支付单。
     *
     * @param current 当前页
     * @param size 每页条数
     * @param tradeState 交易状态，可为空
     * @return 支付单分页
     */
    @Override
    public IPage<MockPaymentDetailResp> page(long current, long size, String tradeState) {
        IPage<MockPaymentOrderDO> page = orderMapper.selectOrderPage(
                new Page<>(current, size),
                StrUtil.emptyToNull(tradeState)
        );
        return page.convert(order -> converter.toDetailResp(order, publicBaseUrl()));
    }

    /**
     * 创建支付单（按商户订单号幂等）。
     *
     * @param appid 公众号 APPID
     * @param mchid 商户号
     * @param description 商品描述
     * @param outTradeNo 商户订单号
     * @param notifyUrl 回调地址
     * @param attach 附加数据
     * @param amountTotal 订单总金额（分）
     * @param currency 币种
     * @param payerOpenid 支付者标识
     * @param tradeType 交易类型
     * @param expireTime 支付截止时间
     * @return 预支付信息
     */
    private MockPaymentCreateResp createOrder(
        String appid,
        String mchid,
        String description,
        String outTradeNo,
        String notifyUrl,
        String attach,
        Integer amountTotal,
        String currency,
        String payerOpenid,
        TradeTypeEnum tradeType,
        LocalDateTime expireTime
    ) {
        Asserts.notNull(amountTotal, BizErrorEnum.MOCK_PAYMENT_AMOUNT_INVALID);
        Asserts.isPositive(amountTotal, BizErrorEnum.MOCK_PAYMENT_AMOUNT_INVALID);
        MockPaymentOrderDO existing = orderMapper.selectByOutTradeNo(outTradeNo);
        if (existing != null) {
            boolean sameAmount = Objects.equals(existing.getAmountTotal(), amountTotal);
            boolean sameDescription = Objects.equals(existing.getDescription(), description);
            Asserts.isTrue(sameAmount && sameDescription, BizErrorEnum.MOCK_PAYMENT_OUT_TRADE_NO_USED);
            return converter.toCreateResp(existing, publicBaseUrl());
        }
        LocalDateTime now = LocalDateTime.now();
        MockPaymentOrderDO order = MockPaymentOrderDO
                .builder()
                .outTradeNo(outTradeNo)
                .appid(appid)
                .mchid(mchid)
                .description(description)
                .notifyUrl(notifyUrl)
                .attach(attach)
                .amountTotal(amountTotal)
                .currency(currency)
                .payerOpenid(payerOpenid)
                .tradeType(tradeType.getCode())
                .tradeState(TradeStateEnum.NOTPAY.getCode())
                .tradeStateDesc(TradeStateEnum.NOTPAY.getDesc())
                .prepayId(tradeType == TradeTypeEnum.JSAPI ? generatePrepayId() : null)
                .codeUrl(tradeType == TradeTypeEnum.NATIVE ? generateCodeUrl() : null)
                .expireTime(
                        expireTime == null
                                ? now.plusMinutes(properties.getDefaultExpireMinutes())
                                : expireTime
                )
                .build();
        orderMapper.insert(order);
        return converter.toCreateResp(order, publicBaseUrl());
    }

    /**
     * 关闭支付单。
     *
     * @param outTradeNo 商户订单号
     */
    private void doClose(String outTradeNo) {
        MockPaymentOrderDO order = requireOrder(outTradeNo);
        if (TradeStateEnum.SUCCESS.getCode().equals(order.getTradeState())) {
            throw new BizException(BizErrorEnum.MOCK_PAYMENT_ORDER_PAID);
        }
        if (TradeStateEnum.CLOSED.getCode().equals(order.getTradeState())) {
            throw new BizException(BizErrorEnum.MOCK_PAYMENT_ORDER_CLOSED);
        }
        int updated = orderMapper.markClosed(
                outTradeNo,
                TradeStateEnum.CLOSED.getCode(),
                TradeStateEnum.CLOSED.getDesc()
        );
        if (updated == 0) {
            MockPaymentOrderDO latest = requireOrder(outTradeNo);
            Asserts.isFalse(
                    TradeStateEnum.SUCCESS.getCode().equals(latest.getTradeState()),
                    BizErrorEnum.MOCK_PAYMENT_ORDER_PAID
            );
            Asserts.isFalse(
                    TradeStateEnum.CLOSED.getCode().equals(latest.getTradeState()),
                    BizErrorEnum.MOCK_PAYMENT_ORDER_CLOSED
            );
            throw new BizException(BizErrorEnum.INVALID_ORDER_STATUS);
        }
    }

    /**
     * 若未支付订单已过期则关闭。
     *
     * @param order 支付单
     */
    private void refreshIfExpired(MockPaymentOrderDO order) {
        boolean notPayAndExpired = order.isExpiredAt(LocalDateTime.now());
        if (!notPayAndExpired) {
            return;
        }
        int updated = orderMapper.markClosed(
                order.getOutTradeNo(),
                TradeStateEnum.CLOSED.getCode(),
                TradeStateEnum.CLOSED.getDesc()
        );
        if (updated > 0) {
            order.setTradeState(TradeStateEnum.CLOSED.getCode());
            order.setTradeStateDesc(TradeStateEnum.CLOSED.getDesc());
        }
    }

    /**
     * 按商户订单号查询支付单，不存在时抛出业务异常。
     *
     * @param outTradeNo 商户订单号
     * @return 支付单
     */
    private MockPaymentOrderDO requireOrder(String outTradeNo) {
        return Asserts.requireNonNull(
                orderMapper.selectByOutTradeNo(outTradeNo),
                BizErrorEnum.MOCK_PAYMENT_ORDER_NOT_EXIST
        );
    }

    /**
     * 校验下单商户信息。
     *
     * @param appid 公众号 APPID
     * @param mchid 商户号
     */
    private void validateMerchant(String appid, String mchid) {
        Asserts.isEqual(appid, properties.getAppid(), BizErrorEnum.INVALID_PARAM);
        Asserts.isEqual(mchid, properties.getMchid(), BizErrorEnum.INVALID_PARAM);
    }

    /**
     * 校验查询/关单请求的商户号。
     *
     * @param mchid 商户号
     */
    private void validateMchid(String mchid) {
        Asserts.isEqual(mchid, properties.getMchid(), BizErrorEnum.INVALID_PARAM);
    }

    /**
     * 解析 RFC3339 支付截止时间。
     *
     * @param timeExpire RFC3339 时间
     * @return 本地时间，空值返回 null
     */
    private LocalDateTime parseExpire(String timeExpire) {
        if (StrUtil.isBlank(timeExpire)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(timeExpire)
                                 .atZoneSameInstant(ZoneId.systemDefault())
                                 .toLocalDateTime();
        } catch (DateTimeParseException e) {
            throw new BizException(BizErrorEnum.INVALID_PARAM);
        }
    }

    /**
     * 对外基础地址。
     *
     * @return 去除末尾斜杠的基础地址
     */
    private String publicBaseUrl() {
        return StrUtil.removeSuffix(properties.getPublicBaseUrl(), "/");
    }

    /**
     * 生成 JSAPI 预支付会话标识。
     *
     * @return 预支付会话标识
     */
    private static String generatePrepayId() {
        return "mock_prepay_" + IdUtil.fastSimpleUUID();
    }

    /**
     * 生成 Native 二维码链接。
     *
     * @return 二维码链接
     */
    private static String generateCodeUrl() {
        return "weixin://wxpay/bizpayurl?pr=" + StrUtil.sub(IdUtil.fastSimpleUUID(), 0, 16);
    }

    /**
     * 生成模拟微信支付订单号。
     *
     * @return 模拟微信支付订单号
     */
    private static String generateTransactionId() {
        return "MOCK" + System.currentTimeMillis() + RandomUtil.randomNumbers(6);
    }
}
