package com.cloudmall.product.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.common.model.CursorReq;
import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.product.model.entity.ProductDO;
import com.cloudmall.product.model.req.ProductGetReq;

/**
 * 商品 SPU Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

    /**
     * 按查询条件获取单个 SPU。 <p>
     * 条件由 {@link ProductGetReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 查询条件
     * @return SPU 实体，未命中返回 null
     */
    default ProductDO selectByGetReq(ProductGetReq req) {
        return selectOne(req.buildLambdaWrapper());
    }

    /**
     * 按 ID 批量查询已上架商品。
     *
     * @param ids SPU ID 集合
     * @return 已上架商品列表
     */
    default List<ProductDO> selectEnabledByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectList(
                Wrappers.<ProductDO>lambdaQuery()
                        .in(ProductDO::getId, ids)
                        .eq(ProductDO::getStatus, StatusEnum.ENABLED)
        );
    }

    /**
     * 按分类 ID 集合查询已上架商品，按创建时间倒序。 <p>
     * 分类 ID 集合可包含指定分类及其子分类（首页分类展示按标签聚合查询）。
     *
     * @param categoryIds 分类 ID 集合
     * @param limit 查询条数上限
     * @return 已上架商品列表
     */
    default List<ProductDO> selectEnabledByCategoryIds(Collection<Long> categoryIds, int limit) {
        if (CollUtil.isEmpty(categoryIds)) {
            return Collections.emptyList();
        }
        return selectList(
                Wrappers.<ProductDO>lambdaQuery()
                        .in(ProductDO::getCategoryId, categoryIds)
                        .eq(ProductDO::getStatus, StatusEnum.ENABLED)
                        .orderByDesc(ProductDO::getCreateTime)
                        .last(LimitEnum.of(limit))
        );
    }

    /**
     * 游标拉取商品搜索文档，支持 ID 游标与时间游标两种模式。 <p>
     * 每次查询 querySize 条，由 Service 层据此判断是否还有更多数据。
     * cursorTime 非空走时间游标（按更新时间升序），否则走 ID 游标（按 ID 升序）。
     *
     * @param req 游标分页参数（cursor / cursorTime 二选一）
     * @param querySize 查询条数（含多查的 1 条）
     * @return 商品记录列表
     */
    default List<ProductDO> selectSearchDocsByCursor(CursorReq req, int querySize) {
        LambdaQueryWrapper<ProductDO> wrapper = Wrappers.lambdaQuery();
        if (req.getCursorTime() != null) {
            wrapper.gt(ProductDO::getUpdateTime, req.getCursorTime());
            wrapper.orderByAsc(ProductDO::getUpdateTime);
        } else {
            wrapper.gt(ProductDO::getId, ObjectUtil.defaultIfNull(req.getCursor(), 0L));
            wrapper.orderByAsc(ProductDO::getId);
        }
        return selectPage(new Page<>(1, querySize), wrapper).getRecords();
    }
}
