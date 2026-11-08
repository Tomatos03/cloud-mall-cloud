package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.UserSecurityDO;

/**
 * 用户账户安全信息 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserSecurityMapper extends BaseMapper<UserSecurityDO> {
}
