package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.query.LambdaWrappers;
import com.cloudmall.mybatisplus.query.Pages;
import com.cloudmall.product.api.model.req.BannerPageReq;
import com.cloudmall.product.api.model.req.BannerReq;
import com.cloudmall.product.model.entity.BannerDO;

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
        return selectList(LambdaWrappers.of(req, BannerDO.class).orderByAsc(BannerDO::getSort));
    }

    /**
     * 分页查询 Banner（条件由 {@link BannerPageReq} 的
     *
     * @QueryCondition 注解声明）。
     * @param req 分页 + 筛选条件
     * @return Banner 分页数据
     */
    default IPage<BannerDO> selectBannerPage(BannerPageReq req) {
        return selectPage(
                Pages.from(req),
                LambdaWrappers.of(req, BannerDO.class).orderByAsc(BannerDO::getSort)
        );
    }
}
