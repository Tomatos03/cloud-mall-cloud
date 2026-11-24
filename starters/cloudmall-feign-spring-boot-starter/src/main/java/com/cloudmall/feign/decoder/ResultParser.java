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
import lombok.RequiredArgsConstructor;

import com.cloudmall.common.model.result.Result;

/**
 * 远程 Result 信封解析器
 * <p>
 * 远程服务统一返回 {@link Result} 信封。本类负责读取响应体、校验信封格式、提取业务状态和反序列化 data，
 * 供正常响应解码器与后续错误解码器共用，避免两处各自解析产生行为漂移。
 *
 * @author : Tomatos
 */
@RequiredArgsConstructor
public class ResultParser {

    private final ObjectMapper objectMapper;

    /**
     * 读取并校验响应中的 Result 信封。
     *
     * @param response Feign 响应
     * @return 信封 JSON 节点
     * @throws IOException 读取响应体失败
     * @throws DecodeException 响应体不是合法的 Result 信封
     */
    public JsonNode parse(Response response) throws IOException {
        String json = readBody(response);
        if (json.isBlank()) {
            throw decodeError(response, "远程响应体为空");
        }

        JsonNode envelope = objectMapper.readTree(json);
        if (envelope == null || !envelope.isObject() || !envelope.has("code")) {
            throw decodeError(response, "远程响应不是有效的 Result 信封: " + json);
        }
        return envelope;
    }

    /**
     * 判断业务码是否成功。
     *
     * @param envelope 信封
     * @return 是否成功
     */
    public boolean success(JsonNode envelope) {
        return code(envelope) == Result.SUCCESS_CODE;
    }

    /**
     * 提取业务码，缺失时按内部错误处理。
     *
     * @param envelope 信封
     * @return 业务码
     */
    public int code(JsonNode envelope) {
        return envelope.path("code").asInt(Result.INTERNAL_ERROR_CODE);
    }

    /**
     * 提取错误消息。
     *
     * @param envelope 信封
     * @param defaultMessage 缺省消息
     * @return 消息
     */
    public String message(JsonNode envelope, String defaultMessage) {
        JsonNode node = envelope.get("message");
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return defaultMessage;
        }
        return node.asText();
    }

    /**
     * 将信封中的 data 反序列化为目标类型。
     *
     * @param envelope 信封
     * @param type 目标类型
     * @return 反序列化结果，data 为空时返回 null
     * @throws IOException 反序列化失败
     */
    public Object data(JsonNode envelope, Type type) throws IOException {
        JsonNode data = envelope.get("data");
        if (data == null || data.isNull()) {
            return null;
        }

        ObjectReader reader = objectMapper.readerFor(objectMapper.constructType(type));
        return reader.readValue(data);
    }

    private DecodeException decodeError(Response response, String message) {
        return new DecodeException(response.status(), message, response.request());
    }

    private String readBody(Response response) throws IOException {
        Response.Body body = response.body();
        if (body == null) {
            return "";
        }

        try (InputStream stream = body.asInputStream(); BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
