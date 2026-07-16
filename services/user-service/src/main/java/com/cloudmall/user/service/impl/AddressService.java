package com.cloudmall.user.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.service.IAddressService;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressResp> listByUserId(Long userId) {
        List<AddressDO> list = addressMapper.selectList(
            Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getUserId, userId)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public AddressResp getById(Long id) {
        AddressDO addr = addressMapper.selectById(id);
        AssertUtils.notNull(addr, BizErrorCode.DATA_NOT_FOUND);
        return toResponse(addr);
    }

    @Override
    public Long create(CreateReq request, Long userId) {
        AddressDO addr = AddressDO.builder()
                .userId(userId)
                .consignee(request.getConsignee())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detail(request.getDetail())
                .zipCode(request.getZipCode())
                .isDefault(request.getIsDefault() != null && request.getIsDefault())
                .build();
        addressMapper.insert(addr);
        return addr.getId();
    }

    @Override
    public void update(AddressUpdateReq request) {
        AddressDO addr = addressMapper.selectById(request.getId());
        AssertUtils.notNull(addr, BizErrorCode.DATA_NOT_FOUND);
        addr.setConsignee(request.getConsignee());
        addr.setPhone(request.getPhone());
        addr.setProvince(request.getProvince());
        addr.setCity(request.getCity());
        addr.setDistrict(request.getDistrict());
        addr.setDetail(request.getDetail());
        addr.setZipCode(request.getZipCode());
        addr.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        addressMapper.updateById(addr);
    }

    @Override
    public void delete(Long id) {
        AddressDO addr = addressMapper.selectById(id);
        AssertUtils.notNull(addr, BizErrorCode.DATA_NOT_FOUND);
        addressMapper.deleteById(id);
    }

    private AddressResp toResponse(AddressDO addr) {
        return AddressResp.builder()
                .id(addr.getId())
                .userId(addr.getUserId())
                .consignee(addr.getConsignee())
                .phone(addr.getPhone())
                .province(addr.getProvince())
                .city(addr.getCity())
                .district(addr.getDistrict())
                .detail(addr.getDetail())
                .zipCode(addr.getZipCode())
                .isDefault(addr.getIsDefault())
                .build();
    }
}
