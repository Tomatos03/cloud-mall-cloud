package com.cloudmall.user.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.model.resp.RegionResp;
import com.cloudmall.user.service.RegionService;

/**
 * 行政区划查询接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/regions")
public class RegionController {

    private final RegionService regionService;

    /**
     * 查询下级区域。
     *
     * @param parentCode 父级代码，不传返回省级
     * @return 区域列表
     */
    @GetMapping("/children")
    public Result<List<RegionResp>> listChildren(
        @RequestParam(value = "parentCode", required = false) String parentCode
    ) {
        return Result.success(regionService.listChildren(parentCode));
    }

    /**
     * 查询区域路径。
     *
     * @param code 区域代码
     * @return 路径列表
     */
    @GetMapping("/path")
    public Result<List<RegionResp>> getPath(@RequestParam("code") String code) {
        return Result.success(regionService.getPath(code));
    }
}
