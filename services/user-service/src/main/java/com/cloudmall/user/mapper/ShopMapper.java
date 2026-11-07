package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.ShopDO;
import com.cloudmall.user.model.req.ShopReq;

@Mapper
public interface ShopMapper extends BaseMapper<ShopDO> {

    /**
     * 按查询请求查询店铺（条件由 {@link ShopReq} 的 @QueryCondition 注解
     * 声明）。
     *
     * @param req 筛选条件
     * @return 店铺，不存在时返回 null
     */
    default ShopDO selectByReq(ShopReq req) {
        return selectOne(req.buildLambdaWrapper());
    }
}
