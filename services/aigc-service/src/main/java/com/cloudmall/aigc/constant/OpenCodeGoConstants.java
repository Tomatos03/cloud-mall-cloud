package com.cloudmall.aigc.constant;

/**
 * OpenCode Go 接入常量。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
public interface OpenCodeGoConstants {

    /**
     * OpenCode 用于路由与提示词缓存的会话标识请求头。
     */
    String SESSION_HEADER = "x-opencode-session";

    /**
     * 响应压缩协商请求头。
     */
    String ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    /**
     * 要求服务端返回未压缩响应，避免 Spring RestClient 无法解码 Brotli。
     */
    String ENCODING_IDENTITY = "identity";
}
