package com.cloudmall.user.service;

import com.cloudmall.user.api.request.AddressCreateRequest;
import com.cloudmall.user.api.request.AddressUpdateRequest;
import com.cloudmall.user.api.response.AddressResponse;
import java.util.List;

public interface IAddressService {
    List<AddressResponse> listByUserId(Long userId);
    AddressResponse getById(Long id);
    Long create(AddressCreateRequest request, Long userId);
    void update(AddressUpdateRequest request);
    void delete(Long id);
}
