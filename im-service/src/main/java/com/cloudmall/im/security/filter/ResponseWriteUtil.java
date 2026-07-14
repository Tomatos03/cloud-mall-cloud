package com.cloudmall.im.security.filter;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应写入工具类
 *
 * @author Tomatos
 * @date 2025/12/18
 */
@Slf4j
public class ResponseWriteUtil {

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    public static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeErrorResponse(response, message, HttpStatus.UNAUTHORIZED.value());
    }

    public static void writeForbidden(HttpServletResponse response, String message) throws IOException {
        writeErrorResponse(response, message, HttpStatus.FORBIDDEN.value());
    }

    public static void writeErrorResponse(HttpServletResponse response, String message, int httpStatus) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        writeResponse(response, result, httpStatus);
    }

    private static void writeResponse(HttpServletResponse response, Map<String, Object> result, int httpStatus) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JSONUtil.toJsonStr(result));
        response.getWriter().flush();
    }
}
