package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.entity.ShopDO;
import com.cloudmall.user.model.req.ShopGetReq;

@Mapper
public interface ShopMapper extends BaseMapper<ShopDO> {

    /**
     * 按查询请求查询店铺（条件由 {@link ShopGetReq} 的 @QueryCondition
     * 注解声明）。
     *
     * @param req 筛选条件
     * @return 店铺，不存在时返回 null
     */
    default ShopDO selectByReq(ShopGetReq req) {
        return selectOne(req.buildLambdaWrapper());
    }
}
