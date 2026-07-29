package com.cloudmall.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.model.req.AddressPageReq;

public interface IAddressService {
    IPage<AddressResp> page(AddressPageReq req);
    AddressResp getById(Long id);
    Long create(CreateReq request, Long userId);
    void update(AddressUpdateReq request);
    void delete(Long id);
}