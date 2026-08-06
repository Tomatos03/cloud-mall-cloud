package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.BannerDO;
import com.cloudmall.product.model.req.BannerPageReq;
import com.cloudmall.product.model.req.BannerReq;

@Mapper
public interface BannerMapper extends BaseMapper<BannerDO> {

    /**
     * 查询 Banner 列表（条件由 {@link BannerReq} 的
     *
     * @QueryCondition 注解声明）。
     * @param req 筛选条件
     * @return Banner 列表
     */
    default List<BannerDO> selectByReq(BannerReq req) {
        return selectList(req.buildLambdaWrapper());
    }

    /**
     * 分页查询 Banner（条件由 {@link BannerPageReq} 的
     *
     * @QueryCondition 注解声明）。
     * @param req 分页 + 筛选条件
     * @return Banner 分页数据
     */
    default IPage<BannerDO> selectBannerPage(BannerPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }
}
