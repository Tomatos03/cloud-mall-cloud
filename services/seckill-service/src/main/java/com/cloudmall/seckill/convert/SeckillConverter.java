package com.cloudmall.seckill.convert;

import org.mapstruct.Mapper;

import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;

@Mapper(componentModel = "spring")
public interface SeckillConverter {

    /**
     * 转换对象数据。
     *
     * @param activity 秒杀活动实体
     * @return 处理结果
     */
    ActivityResp toResp(SeckillActivityDO activity);
}
