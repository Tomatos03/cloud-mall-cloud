package com.cloudmall.shared.model.bo;

/**
 * 文件内容元信息，用于流式下载时设置响应头。
 *
 * @param contentType MIME 类型
 * @param size 文件大小（字节），未知时为 -1
 * @author : Tomatos
 */
public record FileContent(String contentType, long size) {
}
