package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.entity.AddressDO;

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
     * 统计用户收货地址数量。
     *
     * @param userId 用户标识
     * @return 收货地址数量
     */
    default Long countByUserId(Long userId) {
        return selectCount(Wrappers.<AddressDO>lambdaQuery().eq(AddressDO::getUserId, userId));
    }
}
