package com.cloudmall.user.service.impl;

import java.util.List;

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
        List<AddressDO> list = addressMapper.selectList(
                Wrappers.<AddressDO>lambdaQuery()
                        .eq(AddressDO::getUserId, userId)
        );
        return addressConverter.toRespList(list);
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public AddressResp getById(Long id) {
        AddressDO address = addressMapper.selectById(id);
        AssertUtils.notNull(address, BizErrorCode.DATA_NOT_FOUND);
        return addressConverter.toResp(address);
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
        long count = addressMapper.selectCount(
                Wrappers.<AddressDO>lambdaQuery()
                        .eq(AddressDO::getUserId, userId)
        );
        AssertUtils.isFalse(count >= MAX_ADDRESS_COUNT, BizErrorCode.ADDRESS_LIMIT_EXCEEDED);

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
        AddressDO address = addressMapper.selectById(request.getId());
        AssertUtils.notNull(address, BizErrorCode.DATA_NOT_FOUND);
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
        AssertUtils.notEmpty(addresses, BizErrorCode.ADDRESS_NOT_EXIST);

        boolean hasDefault = addresses.stream()
                                      .anyMatch(AddressDO::getIsDefault);
        AssertUtils.isFalse(hasDefault, BizErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        addressMapper.deleteByIds(ids);
    }
}
