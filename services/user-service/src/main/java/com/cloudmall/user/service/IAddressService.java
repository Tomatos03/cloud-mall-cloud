package com.cloudmall.user.service;

import java.util.List;

import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;

public interface IAddressService {

    /**
     * 获取用户所有收货地址
     *
     * @param userId 用户 ID
     * @return 地址列表（上限 10 条，无需分页）
     */
    List<AddressResp> list(Long userId);

    AddressResp getById(Long id);

    Long create(CreateReq request, Long userId);

    void update(AddressUpdateReq request);

    void delete(List<Long> ids);
}