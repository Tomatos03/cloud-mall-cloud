package com.cloudmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.product.entity.BrandDO;
import com.cloudmall.product.model.req.BrandPageReq;

/**
 * Brand Mapper
 *
 * @author cloud-mall
 */
@Mapper
public interface BrandMapper extends BaseMapper<BrandDO> {

    /**
     * 分页查询品牌（条件由 {@link BrandPageReq} 的 @QueryCondition
     * 注解声明）。
     *
     * @param req 分页 + 筛选条件
     * @return 品牌分页数据
     */
    default IPage<BrandDO> selectBrandPage(BrandPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }
}
