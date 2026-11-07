package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.user.api.model.req.AddressUpdateReq;
import com.cloudmall.user.api.model.req.CreateReq;
import com.cloudmall.user.api.model.resp.AddressResp;
import com.cloudmall.user.model.entity.AddressDO;

@Mapper(componentModel = "spring")
public interface AddressConverter {

    /**
     * 转换对象数据。
     *
     * @param address 地址实体
     * @return 处理结果
     */
    AddressResp toResp(AddressDO address);

    /**
     * 转换对象数据。
     *
     * @param addresses 地址实体列表
     * @return 数据列表
     */
    List<AddressResp> toRespList(List<AddressDO> addresses);

    /**
     * 转换对象数据。
     *
     * @param req 请求参数
     * @param userId 用户标识
     * @return 处理结果
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    AddressDO toDO(CreateReq req, Long userId);

    /**
     * 更新相关数据。
     *
     * @param req 请求参数
     * @param address 地址实体
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(AddressUpdateReq req, @MappingTarget AddressDO address);
}
