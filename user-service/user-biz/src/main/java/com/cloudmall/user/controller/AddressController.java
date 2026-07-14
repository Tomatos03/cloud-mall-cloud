package com.cloudmall.user.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.request.AddressCreateRequest;
import com.cloudmall.user.api.request.AddressUpdateRequest;
import com.cloudmall.user.api.response.AddressResponse;
import com.cloudmall.user.service.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping
    public Result<List<AddressResponse>> list(@RequestParam Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<AddressResponse> getById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid AddressCreateRequest request,
                                @RequestHeader("X-User-Id") Long userId) {
        return Result.success(addressService.create(request, userId));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid AddressUpdateRequest request) {
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
