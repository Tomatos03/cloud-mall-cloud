package com.cloudmall.user.service.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.req.AddressUpdateReq;
import com.cloudmall.user.api.model.req.CreateReq;
import com.cloudmall.user.api.model.resp.AddressResp;
import com.cloudmall.user.convert.AddressConverter;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.model.entity.AddressDO;
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
     * 创建新地址, 如果请求中 isDefault 为 true 或 用户之前没有创建任何地址，则新地址将
     * 被设置为默认地址。
     *
     * @param request 请求参数
     * @param userId 用户标识
     * @return 结果标识或数值
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateReq request, Long userId) {
        List<Integer> occupiedSlots = addressMapper.listOccupiedSlots(userId);
        boolean makeDefault = BooleanUtil.isTrue(request.getIsDefault()) || CollUtil.isEmpty(occupiedSlots);

        AddressDO address = addressConverter.toDO(request, userId);
        address.setSlot(nextFreeSlot(occupiedSlots));
        address.setIsDefault(makeDefault);
        try {
            addressMapper.insert(address);
        } catch (DuplicateKeyException e) {
            throw new BizException(BizErrorEnum.ADDRESS_CREATE_FAILED);
        }

        if (makeDefault) {
            addressMapper.setDefault(userId, address.getId());
        }
        return address.getId();
    }

    /**
     * 从已占用槽位中找出最小空闲槽位。
     *
     * @param occupiedSlots 已占用槽位列表
     * @return 空闲槽位
     */
    private int nextFreeSlot(List<Integer> occupiedSlots) {
        Set<Integer> used = CollStreamUtil.toSet(occupiedSlots, Function.identity());
        Integer freeSlot = null;
        for (int slot = 1; slot <= MAX_ADDRESS_COUNT; slot++) {
            if (!used.contains(slot)) {
                freeSlot = slot;
                break;
            }
        }
        Asserts.notNull(freeSlot, BizErrorEnum.ADDRESS_LIMIT_EXCEEDED);
        return freeSlot;
    }

    /**
     * 更新相关数据。
     *
     * @param request 请求参数
     * @param userId 用户 ID
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AddressUpdateReq request, Long userId) {
        AddressDO address = addressConverter.toUpdateDO(request);
        int updated = addressMapper.updateByIdAndUserId(address, request.getId(), userId);
        Asserts.isTrue(updated > 0, BizErrorEnum.ADDRESS_NOT_EXIST);

        if (BooleanUtil.isTrue(request.getIsDefault())) {
            addressMapper.setDefault(userId, request.getId());
        } else if (request.getIsDefault() != null) {
            // isDefault = false
            addressMapper.clearDefaultById(userId, request.getId());
        }
    }

    /**
     * 删除相关数据。
     *
     * @param ids 对象标识列表
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        List<Long> distinctIds = CollUtil.distinct(ids);
        int deleted = addressMapper.deleteNonDefaultByIds(distinctIds);
        Asserts.isTrue(deleted == distinctIds.size(), BizErrorEnum.INVALID_DELETE_DATA);
    }
}
