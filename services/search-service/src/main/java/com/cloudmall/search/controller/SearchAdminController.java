package com.cloudmall.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.search.model.RebuildResultResp;
import com.cloudmall.search.service.ISearchService;

/**
 * 搜索管理 Controller
 * <p>
 * 管理端操作（全量重建等），需通过网关鉴权后访问。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/search/admin")
public class SearchAdminController {

    private final ISearchService searchService;

    /**
     * 全量重建商品索引。
     *
     * @return 重建统计结果
     */
    @PostMapping("/rebuild")
    public Result<RebuildResultResp> rebuild() {
        return Result.success(searchService.rebuild());
    }
}
