package com.cloudmall.im.service;

import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.im.model.resp.ChatFileResp;

/**
 * 聊天文件服务
 *
 * @author : Tomatos
 * @date : 2026/11
 */
public interface IChatFileService {

    /**
     * 保存聊天图片或视频文件。
     *
     * @param file 上传文件
     * @return 文件访问信息
     */
    ChatFileResp upload(MultipartFile file);
}
