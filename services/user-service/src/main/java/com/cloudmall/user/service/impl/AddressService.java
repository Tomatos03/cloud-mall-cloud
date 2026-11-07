package com.cloudmall.user.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.req.AddressUpdateReq;
import com.cloudmall.user.api.model.req.CreateReq;
import com.cloudmall.user.api.model.resp.AddressResp;
import com.cloudmall.user.convert.AddressConverter;
import com.cloudmall.user.model.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.service.IAddressService;

@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {

    private static final int MAX_ADDRESS_COUNT = 10;

    private final AddressMapper addressMapper;
    private final AddressConverter addressConverter;

    /**
     * 查询数据列表。
     *
     * @param userId 用户标识
     * @return 数据列表
     */
    @Override
    public List<AddressResp> list(Long userId) {
        List<AddressDO> list = addressMapper.listByUserId(userId);
        return addressConverter.toRespList(list);
    }

    /**
     * 获取用户指定收货地址（不存在或不属于该用户时返回 null）。
     *
     * @param id 地址 ID
     * @param userId 用户 ID
     * @return 地址响应
     */
    @Override
    public AddressResp getById(Long id, Long userId) {
        return addressConverter.toResp(addressMapper.selectByIdAndUserId(id, userId));
    }

    /**
     * 创建并保存数据。
     *
     * @param request 请求参数
     * @param userId 用户标识
     * @return 结果标识或数值
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public Long create(CreateReq request, Long userId) {
        long count = addressMapper.countByUserId(userId);
        Asserts.isFalse(count >= MAX_ADDRESS_COUNT, BizErrorEnum.ADDRESS_LIMIT_EXCEEDED);

        AddressDO address = addressConverter.toDO(request, userId);
        addressMapper.insert(address);
        return address.getId();
    }

    /**
     * 更新相关数据。
     *
     * @param request 请求参数
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public void update(AddressUpdateReq request) {
        AddressDO address = Asserts.requireNonNull(addressMapper.selectById(request.getId()));
        addressConverter.updateEntity(request, address);
        addressMapper.updateById(address);
    }

    /**
     * 删除相关数据。
     *
     * @param ids 对象标识列表
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public void delete(List<Long> ids) {
        List<AddressDO> addresses = addressMapper.selectBatchIds(ids);
        Asserts.notEmpty(addresses, BizErrorEnum.ADDRESS_NOT_EXIST);

        boolean hasDefault = addresses.stream().anyMatch(AddressDO::getIsDefault);
        Asserts.isFalse(hasDefault, BizErrorEnum.ADDRESS_DEFAULT_CANNOT_DELETE);
        addressMapper.deleteByIds(ids);
    }
}
