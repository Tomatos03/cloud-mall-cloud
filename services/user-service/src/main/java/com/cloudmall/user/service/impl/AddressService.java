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
import com.cloudmall.user.convert.AddressConverter;
import com.cloudmall.user.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.service.IAddressService;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressMapper addressMapper;
    private final AddressConverter addressConverter;

    @Override
    public List<AddressResp> listByUserId(Long userId) {
        List<AddressDO> list = addressMapper.selectList(
            Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getUserId, userId)
        );
        return list.stream().map(addressConverter::toResp).collect(Collectors.toList());
    }

    @Override
    public AddressResp getById(Long id) {
        AddressDO addr = addressMapper.selectById(id);
        AssertUtils.notNull(addr, BizErrorCode.DATA_NOT_FOUND);
        return addressConverter.toResp(addr);
    }

    @Override
    public Long create(CreateReq request, Long userId) {
        AddressDO addr = addressConverter.toDO(request, userId);
        addressMapper.insert(addr);
        return addr.getId();
    }

    @Override
    public void update(AddressUpdateReq request) {
        AddressDO addr = addressMapper.selectById(request.getId());
        AssertUtils.notNull(addr, BizErrorCode.DATA_NOT_FOUND);
        addressConverter.updateEntity(request, addr);
        addressMapper.updateById(addr);
    }

    @Override
    public void delete(Long id) {
        AddressDO addr = addressMapper.selectById(id);
        AssertUtils.notNull(addr, BizErrorCode.DATA_NOT_FOUND);
        addressMapper.deleteById(id);
    }
}
