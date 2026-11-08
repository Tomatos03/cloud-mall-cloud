package com.cloudmall.mockpayment.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 缓存请求体的 HttpServletRequest 包装器。
 * <p>
 * 验签与后续 JSON 反序列化需要读取同一份原始报文，包装后允许重复读取。
 *
 * @author : Tomatos
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] body;

    /**
     * 构造请求包装器。
     *
     * @param request 原始请求
     * @param body 请求体字节
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request, byte[] body) {
        super(request);
        this.body = body;
    }

    /**
     * 获取缓存的请求体输入流。
     *
     * @return 输入流
     */
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 同步读取场景不需要异步监听
            }

            @Override
            public int read() {
                return inputStream.read();
            }
        };
    }

    /**
     * 获取缓存的请求体读取器。
     *
     * @return 读取器
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * 获取缓存的请求体。
     *
     * @return 请求体字节
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * 获取缓存的请求体文本。
     *
     * @return 请求体文本
     */
    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * 读取当前请求体（仅用于包装前）。
     *
     * @param request 原始请求
     * @return 请求体字节
     * @throws IOException 读取失败
     */
    static byte[] readBody(HttpServletRequest request) throws IOException {
        return request.getInputStream().readAllBytes();
    }
}
