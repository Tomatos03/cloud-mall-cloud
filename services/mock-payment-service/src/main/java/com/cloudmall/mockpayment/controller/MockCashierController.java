package com.cloudmall.mockpayment.controller;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.cloudmall.common.exception.BizException;
import com.cloudmall.mockpayment.model.resp.MockPaymentDetailResp;
import com.cloudmall.mockpayment.service.IWechatPayService;

/**
 * 模拟微信支付收银台页面。
 * <p>
 * 浏览器打开后展示支付单信息，点击按钮即可模拟用户支付成功并触发回调，
 * 是本地联调最直观的入口。
 *
 * @author : Tomatos
 */
@Controller
@RequiredArgsConstructor
public class MockCashierController {

    private final IWechatPayService wechatPayService;

    /**
     * 渲染模拟收银台。
     *
     * @param outTradeNo 商户订单号
     * @return 收银台 HTML
     */
    @ResponseBody
    @GetMapping(value = "/mock/cashier/{outTradeNo}", produces = MediaType.TEXT_HTML_VALUE)
    public String cashier(@PathVariable("outTradeNo") String outTradeNo) {
        try {
            return render(wechatPayService.detail(outTradeNo));
        } catch (BizException e) {
            return errorPage(e.getMessage());
        }
    }

    /**
     * 渲染支付单页面。
     *
     * @param detail 支付单详情
     * @return HTML
     */
    private String render(MockPaymentDetailResp detail) {
        String amount = BigDecimal.valueOf(detail.getAmountTotal() == null ? 0 : detail.getAmountTotal())
                                  .movePointLeft(2)
                                  .toPlainString();
        String action = actionHtml(detail);
        return TEMPLATE.replace("__ORDER_NO__", HtmlUtils.htmlEscape(detail.getOutTradeNo()))
                       .replace("__DESCRIPTION__", HtmlUtils.htmlEscape(detail.getDescription()))
                       .replace("__AMOUNT__", amount)
                       .replace("__STATE__", HtmlUtils.htmlEscape(detail.getTradeStateDesc()))
                       .replace("__ACTION__", action);
    }

    /**
     * 构造页面操作区。
     *
     * @param detail 支付单详情
     * @return 操作区 HTML
     */
    private String actionHtml(MockPaymentDetailResp detail) {
        return switch (detail.getTradeState()) {
            case "SUCCESS" -> """
                    <div class="state state-success">该支付单已支付，回调已发送</div>
                    """;
            case "CLOSED" -> """
                    <div class="state state-closed">该支付单已关闭</div>
                    """;
            default -> """
                    <button id="payBtn" onclick="pay()">模拟支付成功</button>
                    <p class="tip" id="result"></p>
                    """;
        };
    }

    /**
     * 渲染错误页面。
     *
     * @param message 错误信息
     * @return HTML
     */
    private String errorPage(String message) {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>模拟微信支付收银台</title>
                  <style>
                    body { margin: 0; min-height: 100vh; display: grid;
                           place-items: center; background: #f4f6f8;
                           font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
                    .card { width: min(420px, 90vw); background: #fff; border-radius: 18px;
                            padding: 32px; box-shadow: 0 18px 50px rgba(15, 23, 42, .08);
                            text-align: center; }
                    h1 { font-size: 20px; color: #1f2937; }
                    p { color: #6b7280; line-height: 1.7; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>支付单不存在</h1>
                    <p>__MESSAGE__</p>
                  </div>
                </body>
                </html>
                """.replace("__MESSAGE__", HtmlUtils.htmlEscape(message));
    }

    /** 收银台页面模板 */
    private static final String TEMPLATE = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>模拟微信支付收银台</title>
              <style>
                body { margin: 0; min-height: 100vh; display: grid; place-items: center;
                       background: #f4f6f8; color: #1f2937;
                       font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
                .card { width: min(440px, 92vw); background: #fff; border-radius: 18px;
                        padding: 32px; box-shadow: 0 18px 50px rgba(15, 23, 42, .08); }
                .brand { color: #07c160; font-weight: 700; letter-spacing: .04em; }
                h1 { margin: 10px 0 6px; font-size: 22px; }
                .desc { color: #6b7280; font-size: 14px; margin: 0 0 24px; }
                .amount { font-size: 44px; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 24px; }
                .meta { display: flex; justify-content: space-between; gap: 20px;
                        padding: 10px 0; border-top: 1px solid #f0f2f5;
                        font-size: 13px; color: #6b7280; }
                .meta span:last-child { color: #374151;
                        font-family: ui-monospace, SFMono-Regular, monospace; }
                button { width: 100%; margin-top: 24px; border: none; border-radius: 10px;
                         padding: 14px; background: #07c160; color: #fff;
                         font-size: 16px; font-weight: 700; cursor: pointer; }
                button:disabled { opacity: .6; cursor: wait; }
                .state { margin-top: 24px; border-radius: 10px; padding: 14px; text-align: center;
                         font-weight: 700; }
                .state-success { background: #ecfdf3; color: #067647; }
                .state-closed { background: #f3f4f6; color: #6b7280; }
                .tip { margin: 18px 0 0; color: #9ca3af; font-size: 12px; line-height: 1.7; }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="brand">微信支付 · MOCK</div>
                <h1>模拟收银台</h1>
                <p class="desc">__DESCRIPTION__</p>
                <div class="amount">¥__AMOUNT__</div>
                <div class="meta"><span>商户订单号</span><span>__ORDER_NO__</span></div>
                <div class="meta"><span>交易状态</span><span>__STATE__</span></div>
                <input type="hidden" id="orderNo" value="__ORDER_NO__">
                __ACTION__
                <p class="tip">这是本地模拟支付，不会产生真实扣款。点击支付成功后会向 notify_url
                  发送微信支付风格加密回调。</p>
              </div>
              <script>
                async function pay() {
                  const input = document.getElementById('orderNo');
                  const button = document.getElementById('payBtn');
                  const result = document.getElementById('result');
                  button.disabled = true;
                  button.textContent = '处理中...';
                  try {
                    const response = await fetch(
                      '/mock/payments/' + encodeURIComponent(input.value) + '/pay',
                      { method: 'POST' }
                    );
                    const data = await response.json();
                    if (response.ok && data.code === 0) {
                      window.location.reload();
                      return;
                    }
                    result.textContent = '支付失败：' + (data.message || '未知错误');
                  } catch (error) {
                    result.textContent = '支付失败：' + error.message;
                  }
                  button.disabled = false;
                  button.textContent = '模拟支付成功';
                }
              </script>
            </body>
            </html>
            """;
}
