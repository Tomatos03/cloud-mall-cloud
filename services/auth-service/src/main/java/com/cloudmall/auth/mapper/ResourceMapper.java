package com.cloudmall.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.auth.entity.ResourceDO;

/**
 * 资源权限 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Mapper
public interface ResourceMapper extends BaseMapper<ResourceDO> {
}
