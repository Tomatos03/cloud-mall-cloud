package com.cloudmall.user.mapper;

import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.AddressDO;

@Mapper
public interface AddressMapper extends BaseMapper<AddressDO> {

    /**
     * 查询用户全部收货地址。
     *
     * @param userId 用户标识
     * @return 收货地址列表
     */
    default List<AddressDO> listByUserId(Long userId) {
        return selectList(Wrappers.<AddressDO>lambdaQuery().eq(AddressDO::getUserId, userId));
    }

    /**
     * 查询用户已占用的地址槽位。
     *
     * @param userId 用户 ID
     * @return 已占用的槽位列表
     */
    default List<Integer> listOccupiedSlots(Long userId) {
        List<AddressDO> addresses = selectList(
                Wrappers.<AddressDO>lambdaQuery()
                        .select(AddressDO::getSlot)
                        .eq(AddressDO::getUserId, userId)
        );
        return CollStreamUtil.toList(addresses, AddressDO::getSlot);
    }

    /**
     * 按 ID 与用户标识更新地址字段，仅更新实体中非 null 的字段。
     *
     * @param address 包含待更新字段的实体
     * @param id 地址 ID
     * @param userId 用户 ID
     * @return 影响行数
     */
    default int updateByIdAndUserId(AddressDO address, Long id, Long userId) {
        return update(
                address,
                Wrappers.<AddressDO>lambdaUpdate()
                        .eq(AddressDO::getId, id)
                        .eq(AddressDO::getUserId, userId)
        );
    }

    /**
     * 设置用户默认地址，单条语句完成清旧设新。
     *
     * @param userId 用户 ID
     * @param addressId 需要设为默认的地址 ID
     * @return 影响行数
     */
    default int setDefault(Long userId, Long addressId) {
        final String defaultSql = "is_default = CASE WHEN id = {0} THEN 1 ELSE 0 END";
        return update(
                Wrappers.<AddressDO>lambdaUpdate()
                        .setSql(defaultSql, addressId)
                        .set(AddressDO::getUpdateTime, LocalDateTime.now())
                        .eq(AddressDO::getUserId, userId)
                        .and(
                                wrapper -> wrapper.eq(AddressDO::getIsDefault, true)
                                                  .or()
                                                  .eq(AddressDO::getId, addressId)
                        )
        );
    }

    /**
     * 清除指定地址的默认标记。
     *
     * @param userId 用户 ID
     * @param addressId 地址 ID
     * @return 影响行数
     */
    default int clearDefaultById(Long userId, Long addressId) {
        return update(
                Wrappers.<AddressDO>lambdaUpdate()
                        .set(AddressDO::getIsDefault, false)
                        .set(AddressDO::getUpdateTime, LocalDateTime.now())
                        .eq(AddressDO::getUserId, userId)
                        .eq(AddressDO::getId, addressId)
                        .eq(AddressDO::getIsDefault, true)
        );
    }

    /**
     * 按 ID 批量删除非默认地址。
     *
     * @param ids 地址 ID 集合
     * @return 删除条数
     */
    default int deleteNonDefaultByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        return delete(
                Wrappers.<AddressDO>lambdaUpdate()
                        .in(AddressDO::getId, ids)
                        .ne(AddressDO::getIsDefault, true)
        );
    }

    /**
     * 查询用户指定收货地址（归属校验）。
     *
     * @param id 地址 ID
     * @param userId 用户 ID
     * @return 收货地址，不存在或不属于该用户时返回 null
     */
    default AddressDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(
                Wrappers.<AddressDO>lambdaQuery()
                        .eq(AddressDO::getId, id)
                        .eq(AddressDO::getUserId, userId)
        );
    }
}
