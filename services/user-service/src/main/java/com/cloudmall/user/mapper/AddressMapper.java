package com.cloudmall.user.mapper;

import java.util.List;

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
     * 按用户批量查询收货地址。
     *
     * @param userIds 用户 ID 列表
     * @return 命中的收货地址列表
     */
    default List<AddressDO> selectByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return List.of();
        }
        return selectList(Wrappers.<AddressDO>lambdaQuery().in(AddressDO::getUserId, userIds));
    }

    /**
     * 统计用户收货地址数量。
     *
     * @param userId 用户标识
     * @return 收货地址数量
     */
    default Long countByUserId(Long userId) {
        return selectCount(Wrappers.<AddressDO>lambdaQuery().eq(AddressDO::getUserId, userId));
    }

    /**
     * 清除用户其它地址的默认标记。
     *
     * @param userId 用户 ID
     * @param excludeId 需要保留默认标记的地址 ID，可为 null
     * @return 影响行数
     */
    default int clearDefault(Long userId, Long excludeId) {
        return update(
                Wrappers.<AddressDO>lambdaUpdate()
                        .set(AddressDO::getIsDefault, false)
                        .eq(AddressDO::getUserId, userId)
                        .eq(AddressDO::getIsDefault, true)
                        .ne(excludeId != null, AddressDO::getId, excludeId)
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
