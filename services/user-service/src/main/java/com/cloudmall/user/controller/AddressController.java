package com.cloudmall.user.controller;

import java.util.List;

import com.cloudmall.common.context.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudmall.common.model.Result;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.service.IAddressService;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    /**
     * 获取当前用户所有收货地址
     * <p>
     * 地址数量上限 10 条，无需分页。
     */
    @GetMapping("/list")
    public Result<List<AddressResp>> list() {
        Long userId = UserContextHolder.getUserId();
        return Result.success(addressService.list(userId));
    }

    @GetMapping("/{id}")
    public Result<AddressResp> getById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid CreateReq request) {
        return Result.success(addressService.create(request, UserContextHolder.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody @Valid AddressUpdateReq request
    ) {
        request.setId(id);
        addressService.update(request);
        return Result.success(null);
    }

    @DeleteMapping
    public Result<Void> delete(@RequestBody List<Long> ids) {
        addressService.delete(ids);
        return Result.success(null);
    }
}
