package com.cloudmall.product.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.product.model.entity.BrandDO;
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

    /**
     * 按排序值 + ID 复合游标查询启用品牌（升序）。
     *
     * @param cursorSort 上一页最后一条品牌的排序值（首次传 0）
     * @param cursorId 上一页最后一条品牌 ID（首次传 0）
     * @param querySize 查询条数
     * @return 品牌列表
     */
    default List<BrandDO> selectBrandsAfter(Long cursorSort, Long cursorId, int querySize) {
        Page<BrandDO> page = new Page<>(1, querySize, false);
        return selectPage(page, Wrappers.<BrandDO>lambdaQuery()
                                        .eq(BrandDO::getStatus, StatusEnum.ENABLED)
                                        .and(
                                                wrapper -> wrapper
                                                        .gt(BrandDO::getSort, cursorSort)
                                                        .or()
                                                        .nested(
                                                                nested -> nested.eq(
                                                                                        BrandDO::getSort,
                                                                                        cursorSort
                                                                                )
                                                                                .gt(BrandDO::getId, cursorId)
                                                        )
                                        )
                                        .orderByAsc(BrandDO::getSort)
                                        .orderByAsc(BrandDO::getId))
                .getRecords();
    }
}
