package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.user.api.model.resp.RegionResp;
import com.cloudmall.user.model.entity.RegionDO;

/**
 * 行政区划转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface RegionConverter {

    /**
     * 区域实体 -> 响应。
     *
     * @param region 区域实体
     * @return 区域响应
     */
    @Mapping(target = "leaf", expression = "java(region.getLevel() != null && region.getLevel() >= 3)")
    RegionResp toResp(RegionDO region);

    /**
     * 区域列表 -> 响应列表。
     *
     * @param regions 区域列表
     * @return 区域响应列表
     */
    List<RegionResp> toRespList(List<RegionDO> regions);
}
