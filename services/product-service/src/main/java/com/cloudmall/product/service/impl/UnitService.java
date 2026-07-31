package com.cloudmall.product.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.product.api.request.UnitCreateReq;
import com.cloudmall.product.api.request.UnitUpdateReq;
import com.cloudmall.product.api.response.UnitResp;
import com.cloudmall.product.convert.UnitConverter;
import com.cloudmall.product.entity.UnitDO;
import com.cloudmall.product.mapper.UnitMapper;
import com.cloudmall.product.model.req.UnitPageReq;
import com.cloudmall.product.service.IUnitService;

/**
 * 商品单位服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UnitService implements IUnitService {

    private final UnitMapper unitMapper;
    private final UnitConverter unitConverter;

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public UnitResp getById(Long id) {
        UnitDO unit = unitMapper.selectById(id);
        AssertUtils.notNull(unit, BizErrorCode.DATA_NOT_FOUND);
        return unitConverter.toResp(unit);
    }

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<UnitResp> page(UnitPageReq req) {
        IPage<UnitDO> page = unitMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(unitConverter::toResp);
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 结果标识或数值
     */
    @Override
    @Transactional
    public Long create(UnitCreateReq req) {
        UnitDO unit = unitConverter.toDO(req);
        unitMapper.insert(unit);
        return unit.getId();
    }

    /**
     * 更新相关数据。
     *
     * @param id 对象标识
     * @param req 请求参数
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional
    public void update(Long id, UnitUpdateReq req) {
        UnitDO unit = unitMapper.selectById(id);
        AssertUtils.notNull(unit, BizErrorCode.DATA_NOT_FOUND);
        unitConverter.merge(unit, req);
        unitMapper.updateById(unit);
    }

    /**
     * 删除相关数据。
     *
     * @param ids 对象标识列表
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        AssertUtils.notEmpty(ids, BizErrorCode.INVALID_PARAM);
        unitMapper.deleteBatchIds(ids);
    }
}
