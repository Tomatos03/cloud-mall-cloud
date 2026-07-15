package com.cloudmall.user.service;

import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.response.AddressResp;
import java.util.List;

public interface IAddressService {
    List<AddressResp> listByUserId(Long userId);
    AddressResp getById(Long id);
    Long create(CreateReq request, Long userId);
    void update(AddressUpdateReq request);
    void delete(Long id);
}
