package com.cloudmall.shared.model;

import java.util.Set;

/**
 * 文件上传策略。
 * <p>
 * 由调用方按业务场景提供校验规则与路径前缀，统一交给文件服务执行。
 *
 * @param keyPrefix 对象 key 前缀，如 files、chat/files
 * @param allowedExtensions 允许的扩展名（小写，不含点）
 * @param allowedContentTypePrefixes 允许的 MIME 前缀，如 image/
 * @param maxSize 单文件大小上限（字节）
 * @author : Tomatos
 */
public record FileUploadPolicy(
    String keyPrefix,
    Set<String> allowedExtensions,
    Set<String> allowedContentTypePrefixes,
    long maxSize
) {
}
