package com.cloudmall.user.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.service.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping
    public Result<List<AddressResp>> list(@RequestParam Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<AddressResp> getById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid CreateReq request,
                                @RequestHeader("X-User-Id") Long userId) {
        return Result.success(addressService.create(request, userId));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid AddressUpdateReq request) {
        request.setId(id);
        addressService.update(request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success(null);
    }
}
