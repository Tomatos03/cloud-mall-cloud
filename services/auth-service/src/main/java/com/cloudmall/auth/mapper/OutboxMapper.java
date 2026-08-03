package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.OutboxDO;

/**
 * 认证服务本地 Outbox Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface OutboxMapper extends BaseMapper<OutboxDO> {
}
