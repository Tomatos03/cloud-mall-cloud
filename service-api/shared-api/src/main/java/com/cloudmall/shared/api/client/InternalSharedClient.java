package com.cloudmall.shared.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloudmall.shared.api.model.req.FileBindReq;
import com.cloudmall.shared.api.model.req.FileReleaseReq;

/**
 * 公共服务内部文件接口客户端。
 * <p>
 * 业务服务保存/替换文件时调用，用于把文件台账从 PENDING 改为 BOUND 或 ORPHAN。
 *
 * @author : Tomatos
 */
@FeignClient(name = "shared-service", contextId = "internalSharedClient", path = "/internal/shared")
public interface InternalSharedClient {

    /**
     * 将文件标记为已被业务引用。
     *
     * @param req 绑定请求
     */
    @PostMapping("/files/bind")
    void bind(@RequestBody FileBindReq req);

    /**
     * 将文件标记为待回收。
     *
     * @param req 释放请求
     */
    @PostMapping("/files/release")
    void release(@RequestBody FileReleaseReq req);
}
