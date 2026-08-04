package com.cloudmall.feign.decoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;

import com.cloudmall.common.exception.FeignRemoteException;
import com.cloudmall.common.model.Result;

/**
 * Feign Result 自动拆箱解码器
 * <p>
 * 服务端统一返回 {@link Result} 包装，此 Decoder 自动完成拆箱：
 * <ul>
 *     <li>code = SUCCESS → 返回 {@code Result.data}（反序列化为目标类型 {@code T}）</li>
 *     <li>code ≠ SUCCESS → 抛出 {@link FeignRemoteException}，携带远程错误码与消息</li>
 * </ul>
 * 调用端无需手动判断 Result，直接使用返回值。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class ResultUnwrappingDecoder implements Decoder {

    private final ObjectMapper objectMapper;

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException {
        String json = readBody(response);
        JsonNode tree = objectMapper.readTree(json);

        int code = tree.get("code").asInt();
        if (code == Result.SUCCESS_CODE) {
            return deserializeData(tree, type);
        }

        String message = tree.has("message") ? tree.get("message").asText() : "远程服务调用失败";
        throw new FeignRemoteException(code, message);
    }

    private Object deserializeData(JsonNode tree, Type type) throws IOException {
        JsonNode dataNode = tree.get("data");
        if (dataNode == null || dataNode.isNull()) {
            return null;
        }
        ObjectReader reader = objectMapper.readerFor(objectMapper.constructType(type));
        return reader.readValue(dataNode);
    }

    private String readBody(Response response) throws IOException {
        InputStream is = response.body().asInputStream();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        )) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
