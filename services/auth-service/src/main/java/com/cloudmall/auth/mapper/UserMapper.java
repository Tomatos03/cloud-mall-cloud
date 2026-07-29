package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.UserDO;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
