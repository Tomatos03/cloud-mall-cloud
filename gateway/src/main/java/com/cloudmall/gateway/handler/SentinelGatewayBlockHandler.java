package com.cloudmall.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.model.Result;

/**
 * 网关 Sentinel 限流拦截响应
 * <p>
 * 被网关流控规则拦截时返回统一错误信封，HTTP 状态 429，业务码 1005。
 *
 * @author : Tomatos
 */
public class SentinelGatewayBlockHandler implements BlockRequestHandler {

    /**
     * 构造拦截响应。
     *
     * @param exchange HTTP 交换对象
     * @param ex 拦截异常
     * @return 统一错误响应
     */
    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(Result.error(BizErrorEnum.FLOW_CONTROL_BLOCKED)));
    }
}
