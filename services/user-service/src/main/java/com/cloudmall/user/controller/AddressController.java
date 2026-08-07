package com.cloudmall.user.controller;

import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import jakarta.validation.Valid;
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

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.service.IAddressService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {

    private final IAddressService addressService;

    /**
     * 获取当前用户所有收货地址。 <p>
     * 地址数量上限 10 条，无需分页。
     *
     * @return 处理结果
     */
    @GetMapping
    public Result<List<AddressResp>> list() {
        Long userId = UserContextHolder.getUserId();
        return Result.success(addressService.list(userId));
    }

    /**
     * 获取指定收货地址（内部调用可传 userId，用户端缺省取登录态）。
     *
     * @param id 地址 ID
     * @param userId 用户 ID（可选）
     * @return 处理结果，不存在或不属于该用户时返回 null
     */
    @GetMapping("/{id}")
    public Result<AddressResp> getById(
            @PathVariable
            Long id,
            @RequestParam(required = false)
            Long userId
    ) {
        Long ownerId = ObjectUtil.defaultIfNull(userId, UserContextHolder.getUserId());
        return Result.success(addressService.getById(id, ownerId));
    }

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @PostMapping
    public Result<Long> create(
            @RequestBody
            @Valid
            CreateReq request
    ) {
        return Result.success(addressService.create(request, UserContextHolder.getUserId()));
    }

    /**
     * 更新相关数据。
     *
     * @param id 对象标识
     * @param request 请求参数
     * @return 处理结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable
            Long id,
            @RequestBody
            @Valid
            AddressUpdateReq request
    ) {
        request.setId(id);
        addressService.update(request);
        return Result.success(null);
    }

    /**
     * 删除相关数据。
     *
     * @param ids 对象标识列表
     * @return 处理结果
     */
    @DeleteMapping
    public Result<Void> delete(
            @RequestBody
            List<Long> ids
    ) {
        addressService.delete(ids);
        return Result.success(null);
    }
}
