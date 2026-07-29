package com.cloudmall.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.convert.AddressConverter;
import com.cloudmall.user.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.model.req.AddressPageReq;
import com.cloudmall.user.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private final AddressMapper addressMapper;
    private final AddressConverter addressConverter;

    @Override
    public IPage<AddressResp> page(AddressPageReq req) {
        return addressMapper.selectPage(req, req.buildLambdaWrapper())
                            .convert(addressConverter::toResp);
    }

    @Override
    public AddressResp getById(Long id) {
        AddressDO address = addressMapper.selectById(id);
        AssertUtils.notNull(address, BizErrorCode.DATA_NOT_FOUND);
        return addressConverter.toResp(address);
    }

    @Override
    public Long create(CreateReq request, Long userId) {
        AddressDO address = addressConverter.toDO(request, userId);
        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    public void update(AddressUpdateReq request) {
        AddressDO address = addressMapper.selectById(request.getId());
        AssertUtils.notNull(address, BizErrorCode.DATA_NOT_FOUND);
        addressConverter.updateEntity(request, address);
        addressMapper.updateById(address);
    }

    @Override
    public void delete(Long id) {
        AddressDO address = addressMapper.selectById(id);
        AssertUtils.notNull(address, BizErrorCode.DATA_NOT_FOUND);
        addressMapper.deleteById(id);
    }
}
