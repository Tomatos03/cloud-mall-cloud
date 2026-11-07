package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.model.entity.UnitDO;
import com.cloudmall.product.model.req.UnitPageReq;

@Mapper
public interface UnitMapper extends BaseMapper<UnitDO> {

    /**
     * 分页查询单位（条件由 {@link UnitPageReq} 的 @QueryCondition 注
     * 解声明）。
     *
     * @param req 分页 + 筛选条件
     * @return 单位分页数据
     */
    default IPage<UnitDO> selectUnitPage(UnitPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }
}
