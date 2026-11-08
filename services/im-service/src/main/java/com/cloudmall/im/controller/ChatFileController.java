package com.cloudmall.im.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.common.model.Result;
import com.cloudmall.im.model.resp.ChatFileResp;
import com.cloudmall.im.service.IChatFileService;

/**
 * 聊天文件控制器
 *
 * @author : Tomatos
 * @date : 2026/11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/files")
public class ChatFileController {

    private final IChatFileService chatFileService;

    /**
     * 上传聊天图片或视频。
     *
     * @param file 上传文件
     * @return 文件访问信息
     */
    @PostMapping
    public Result<ChatFileResp> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(chatFileService.upload(file));
    }
}
