package com.cloudmall.product.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.product.api.request.BrandCreateReq;
import com.cloudmall.product.api.request.BrandUpdateReq;
import com.cloudmall.product.api.response.BrandResp;
import com.cloudmall.product.convert.BrandConverter;
import com.cloudmall.product.entity.BrandDO;
import com.cloudmall.product.mapper.BrandMapper;
import com.cloudmall.product.model.req.BrandPageReq;
import com.cloudmall.product.service.IBrandService;

/**
 * 品牌服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class BrandService implements IBrandService {

    private final BrandMapper brandMapper;
    private final BrandConverter brandConverter;

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public BrandResp getById(Long id) {
        BrandDO brand = brandMapper.selectById(id);
        AssertUtils.notNull(brand, BizErrorCode.DATA_NOT_FOUND);
        return brandConverter.toResp(brand);
    }

    /**
     * 查询数据列表。
     *
     * @param ids 对象标识列表
     * @return 数据列表
     */
    @Override
    public List<BrandResp> listByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        List<BrandDO> brands = brandMapper.selectList(
                Wrappers.<BrandDO>lambdaQuery().in(BrandDO::getId, ids)
        );
        return brandConverter.toRespList(brands);
    }

    /**
     * 分页查询数据。
     *
     * @param req 请求参数
     * @return 分页数据
     */
    @Override
    public IPage<BrandResp> page(BrandPageReq req) {
        IPage<BrandDO> page = brandMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(brandConverter::toResp);
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 结果标识或数值
     */
    @Override
    @Transactional
    public Long create(BrandCreateReq req) {
        BrandDO brand = brandConverter.toDO(req);
        brandMapper.insert(brand);
        return brand.getId();
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
    public void update(Long id, BrandUpdateReq req) {
        BrandDO brand = brandMapper.selectById(id);
        AssertUtils.notNull(brand, BizErrorCode.DATA_NOT_FOUND);
        brandConverter.merge(brand, req);
        brandMapper.updateById(brand);
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
        brandMapper.deleteBatchIds(ids);
    }
}
