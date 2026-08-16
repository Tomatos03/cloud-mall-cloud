package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.model.req.ResourceReq;
import com.cloudmall.user.model.resp.ResourceNodeResp;
import com.cloudmall.user.model.resp.ResourceResp;
import com.cloudmall.user.service.IResourceService;

/**
 * 资源管理控制器
 * <p>
 * 提供菜单、目录、按钮等资源的增删改查，以及完整资源树的查询。
 * 资源同时承担前端菜单和后端权限标识双重职责。
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/resources")
public class ResourceController {

    private final IResourceService resourceService;

    /**
     * 获取完整资源树（不进行权限过滤） <p>
     * 返回按 parentId 构建的层级菜单结构，包含全部 UI 资源节点， 不根据当前用户权限进行裁剪
     * 。用于资源管理后台的全量展示。 仅包含 menu / catalog / layout 类型的 UI
     * 节点。
     *
     * @return 完整资源树根节点列表
     */
    @GetMapping("/tree")
    public Result<List<ResourceNodeResp>> getFullResourceTree() {
        return Result.success(resourceService.getFullResourceTree());
    }

    /**
     * 查询所有资源列表
     *
     * @param type 按类型过滤（可选，menu / catalog / layout /
     *             button）
     * @return 资源列表
     */
    @GetMapping
    public Result<List<ResourceResp>> listResources(@RequestParam(required = false) String type) {
        return Result.success(resourceService.listResources(type));
    }

    /**
     * 查询单个资源
     *
     * @param id 资源 ID
     * @return 资源信息
     */
    @GetMapping("/{id}")
    public Result<ResourceResp> getResource(@PathVariable Long id) {
        return Result.success(resourceService.getResource(id));
    }

    /**
     * 创建资源
     *
     * @param req 资源信息
     * @return 新资源 ID
     */
    @PostMapping
    public Result<Long> createResource(@RequestBody ResourceReq req) {
        return Result.success(resourceService.createResource(req));
    }

    /**
     * 更新资源。
     *
     * @param id 资源 ID
     * @param req 待更新的资源字段
     * @return 处理结果
     */
    @PutMapping("/{id}")
    public Result<Void> updateResource(@PathVariable Long id, @RequestBody ResourceReq req) {
        resourceService.updateResource(id, req);
        return Result.success();
    }

    /**
     * 删除资源。
     *
     * @param id 资源 ID
     * @return 处理结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success();
    }
}
