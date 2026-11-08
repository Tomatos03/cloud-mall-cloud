package com.cloudmall.user.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务。
 *
 * @author : Tomatos
 */
public interface IFileService {

    /**
     * 保存图片并返回访问路径。
     *
     * @param file 图片文件
     * @return 访问路径
     */
    String uploadImage(MultipartFile file);
}
