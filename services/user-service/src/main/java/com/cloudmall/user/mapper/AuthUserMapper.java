package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.entity.auth.AuthUserDO;

@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUserDO> {
}