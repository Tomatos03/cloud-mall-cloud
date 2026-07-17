package com.cloudmall.seckill.convert;

import org.mapstruct.Mapper;

import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;

@Mapper(componentModel = "spring")
public interface SeckillConverter {

    ActivityResp toResp(SeckillActivityDO activity);
}
