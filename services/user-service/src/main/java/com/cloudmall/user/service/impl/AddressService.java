package com.cloudmall.user.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.convert.AddressConverter;
import com.cloudmall.user.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private static final int MAX_ADDRESS_COUNT = 10;

    private final AddressMapper addressMapper;
    private final AddressConverter addressConverter;

    @Override
    public List<AddressResp> list(Long userId) {
        List<AddressDO> list = addressMapper.selectList(
                Wrappers.<AddressDO>lambdaQuery()
                        .eq(AddressDO::getUserId, userId)
        );
        return list.stream()
                   .map(addressConverter::toResp)
                   .toList();
    }

    @Override
    public AddressResp getById(Long id) {
        AddressDO address = addressMapper.selectById(id);
        AssertUtils.notNull(address, BizErrorCode.DATA_NOT_FOUND);
        return addressConverter.toResp(address);
    }

    @Override
    public Long create(CreateReq request, Long userId) {
        long count = addressMapper.selectCount(
                Wrappers.<AddressDO>lambdaQuery()
                        .eq(AddressDO::getUserId, userId)
        );
        AssertUtils.isFalse(count >= MAX_ADDRESS_COUNT, BizErrorCode.ADDRESS_LIMIT_EXCEEDED);

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
    public void delete(List<Long> ids) {
        List<AddressDO> addresses = addressMapper.selectBatchIds(ids);
        AssertUtils.notEmpty(addresses, BizErrorCode.ADDRESS_NOT_EXIST);

        boolean hasDefault = addresses.stream()
                                      .anyMatch(AddressDO::getIsDefault);
        AssertUtils.isFalse(hasDefault, BizErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        addressMapper.deleteByIds(ids);
    }
}
