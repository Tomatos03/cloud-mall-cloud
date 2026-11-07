package com.cloudmall.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.model.entity.CouponOperationLogDO;
import com.cloudmall.coupon.model.req.CouponOperationLogPageReq;

/**
 * 模板管理流水 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CouponOperationLogMapper extends BaseMapper<CouponOperationLogDO> {

    /**
     * 分页查询模板操作流水，按创建时间倒序。 <p>
     * 条件由 {@link CouponOperationLogPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 操作流水分页数据
     */
    default IPage<CouponOperationLogDO> selectOperationLogPage(CouponOperationLogPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }
}
