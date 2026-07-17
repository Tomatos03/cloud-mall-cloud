package com.cloudmall.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.user.api.request.AddressUpdateReq;
import com.cloudmall.user.api.request.CreateReq;
import com.cloudmall.user.api.response.AddressResp;
import com.cloudmall.user.entity.AddressDO;

@Mapper(componentModel = "spring")
public interface AddressConverter {

    AddressResp toResp(AddressDO address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    AddressDO toDO(CreateReq req, Long userId);

    @Mapping(target = "id", ignore = true)
    void updateEntity(AddressUpdateReq req, @MappingTarget AddressDO address);
}
