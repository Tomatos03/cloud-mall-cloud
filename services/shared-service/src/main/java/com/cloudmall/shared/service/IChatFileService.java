package com.cloudmall.shared.service;

import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.shared.model.resp.ChatFileResp;

/**
 * 聊天附件服务。
 *
 * @author : Tomatos
 */
public interface IChatFileService {

    /**
     * 上传聊天图片或视频。
     *
     * @param file 上传文件
     * @return 文件访问信息
     */
    ChatFileResp upload(MultipartFile file);
}
