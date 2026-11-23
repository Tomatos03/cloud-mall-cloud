package com.cloudmall.shared.model;

/**
 * 文件上传结果。
 *
 * @param id 文件台账 ID
 * @param url 访问地址
 * @author : Tomatos
 */
public record FileUploadResult(Long id, String url) {
}
