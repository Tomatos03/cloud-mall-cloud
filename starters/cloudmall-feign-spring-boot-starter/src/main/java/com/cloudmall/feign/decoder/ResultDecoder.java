package com.cloudmall.feign.decoder;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;

import com.cloudmall.feign.exception.RemoteCallException;

/**
 * Feign Result 自动拆箱解码器
 * <p>
 * 服务端统一返回 {@link com.cloudmall.common.model.result.Result} 包装。在 HTTP 2xx 时：
 * <ul>
 *     <li>code = SUCCESS → 返回 data 反序列化后的目标类型 {@code T}</li>
 *     <li>code ≠ SUCCESS → 抛出 {@link RemoteCallException}，携带远程错误码与消息</li>
 * </ul>
 * 信封读取与校验统一委托 {@link ResultParser}；非 2xx 响应由 Feign 错误解码器处理，不经过本类。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class ResultDecoder implements Decoder {

    private final ResultParser resultParser;

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException {
        JsonNode envelope = resultParser.parse(response);
        if (resultParser.success(envelope)) {
            return resultParser.data(envelope, type);
        }

        throw RemoteExceptionFactory.business(
                response,
                resultParser.code(envelope),
                resultParser.message(envelope, "远程服务调用失败")
        );
    }
}
