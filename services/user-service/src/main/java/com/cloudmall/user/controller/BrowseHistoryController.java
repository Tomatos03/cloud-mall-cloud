package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.resp.BrowseHistoryResp;
import com.cloudmall.user.service.BrowseHistoryService;

/**
 * 当前登录用户商品浏览记录接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/browse-history")
public class BrowseHistoryController {

    private final BrowseHistoryService browseHistoryService;

    /**
     * 查询浏览记录。
     *
     * @return 浏览记录
     */
    @GetMapping
    public Result<List<BrowseHistoryResp>> list() {
        return Result.success(browseHistoryService.list(UserContextHolder.getUserId()));
    }

    /**
     * 记录商品浏览。
     *
     * @param productId 商品 ID
     * @return 浏览记录
     */
    @PostMapping("/{productId}")
    public Result<BrowseHistoryResp> record(@PathVariable Long productId) {
        return Result.success(browseHistoryService.record(UserContextHolder.getUserId(), productId));
    }

    /**
     * 删除单条浏览记录。
     *
     * @param productId 商品 ID
     * @return 处理结果
     */
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        browseHistoryService.remove(UserContextHolder.getUserId(), productId);
        return Result.success();
    }

    /**
     * 清空浏览记录。
     *
     * @return 处理结果
     */
    @DeleteMapping
    public Result<Void> clear() {
        browseHistoryService.clear(UserContextHolder.getUserId());
        return Result.success();
    }
}
