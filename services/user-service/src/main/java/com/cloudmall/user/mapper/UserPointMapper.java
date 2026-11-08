package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.UserPointDO;

/**
 * 用户积分账户 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserPointMapper extends BaseMapper<UserPointDO> {
}
