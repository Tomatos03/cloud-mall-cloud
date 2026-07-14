package com.cloudmall.user.service.impl;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.user.api.request.AddressCreateRequest;
import com.cloudmall.user.api.request.AddressUpdateRequest;
import com.cloudmall.user.api.response.AddressResponse;
import com.cloudmall.user.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponse> listByUserId(Long userId) {
        List<AddressDO> list = addressMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AddressDO>()
                .eq(AddressDO::getUserId, userId)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public AddressResponse getById(Long id) {
        AddressDO addr = addressMapper.selectById(id);
        if (addr == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        return toResponse(addr);
    }

    @Override
    public Long create(AddressCreateRequest request, Long userId) {
        AddressDO addr = new AddressDO();
        addr.setUserId(userId);
        addr.setConsignee(request.getConsignee());
        addr.setPhone(request.getPhone());
        addr.setProvince(request.getProvince());
        addr.setCity(request.getCity());
        addr.setDistrict(request.getDistrict());
        addr.setDetail(request.getDetail());
        addr.setZipCode(request.getZipCode());
        addr.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        addressMapper.insert(addr);
        return addr.getId();
    }

    @Override
    public void update(AddressUpdateRequest request) {
        AddressDO addr = addressMapper.selectById(request.getId());
        if (addr == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
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
        addressMapper.deleteById(id);
    }

    private AddressResponse toResponse(AddressDO addr) {
        AddressResponse r = new AddressResponse();
        r.setId(addr.getId());
        r.setUserId(addr.getUserId());
        r.setConsignee(addr.getConsignee());
        r.setPhone(addr.getPhone());
        r.setProvince(addr.getProvince());
        r.setCity(addr.getCity());
        r.setDistrict(addr.getDistrict());
        r.setDetail(addr.getDetail());
        r.setZipCode(addr.getZipCode());
        r.setIsDefault(addr.getIsDefault());
        return r;
    }
}
