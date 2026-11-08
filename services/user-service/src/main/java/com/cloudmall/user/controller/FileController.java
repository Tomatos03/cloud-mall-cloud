package com.cloudmall.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.model.resp.FileUploadResp;
import com.cloudmall.user.service.IFileService;

/**
 * 文件上传接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final IFileService fileService;

    /**
     * 上传图片。
     *
     * @param file 图片文件
     * @return 访问路径
     */
    @PostMapping("/upload")
    public Result<FileUploadResp> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(FileUploadResp.builder()
                                            .url(fileService.uploadImage(file))
                                            .build());
    }
}
