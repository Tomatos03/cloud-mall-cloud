package com.cloudmall.shared.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.result.Result;
import com.cloudmall.shared.api.model.req.FileBindReq;
import com.cloudmall.shared.api.model.req.FileReleaseReq;
import com.cloudmall.shared.service.IFileService;

/**
 * 公共服务内部文件接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/shared")
public class InternalSharedController {

    private final IFileService fileService;

    /**
     * 将文件标记为已被业务引用。
     *
     * @param req 绑定请求
     * @return 处理结果
     */
    @PostMapping("/files/bind")
    public Result<Void> bind(@RequestBody @Valid FileBindReq req) {
        fileService.bind(req.getOwnerId(), req.getBizType(), req.getUrls());
        return Result.success();
    }

    /**
     * 将文件标记为待回收。
     *
     * @param req 释放请求
     * @return 处理结果
     */
    @PostMapping("/files/release")
    public Result<Void> release(@RequestBody @Valid FileReleaseReq req) {
        fileService.release(req.getUrls());
        return Result.success();
    }
}
