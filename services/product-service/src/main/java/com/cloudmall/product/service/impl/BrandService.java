package com.cloudmall.product.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.model.CursorReq;
import com.cloudmall.common.model.CursorResult;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.model.req.BrandCreateReq;
import com.cloudmall.product.api.model.req.BrandUpdateReq;
import com.cloudmall.product.api.model.resp.BrandResp;
import com.cloudmall.product.convert.BrandConverter;
import com.cloudmall.product.mapper.BrandMapper;
import com.cloudmall.product.model.entity.BrandDO;
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

    /** 品牌复合游标（排序值 + ID）的进位基数 */
    private static final long CURSOR_SORT_FACTOR = 1_000_000_000L;

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
        BrandDO brand = Asserts.requireNonNull(brandMapper.selectById(id));
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
        List<BrandDO> brands = brandMapper.selectByIds(ids);
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
        IPage<BrandDO> page = brandMapper.selectBrandPage(req);
        return page.convert(brandConverter::toResp);
    }

    /**
     * 游标查询启用品牌（按品牌 ID 升序）。
     *
     * @param req 游标请求
     * @return 游标分页结果
     */
    @Override
    public CursorResult<BrandResp> cursor(CursorReq req) {
        int size = ObjectUtil.defaultIfNull(req.getSize(), 10);
        size = Math.min(Math.max(size, 1), CursorReq.MAX_SIZE);
        long cursor = ObjectUtil.defaultIfNull(req.getCursor(), 0L);
        long cursorSort = cursor / CURSOR_SORT_FACTOR;
        long cursorId = cursor % CURSOR_SORT_FACTOR;
        List<BrandDO> records = brandMapper.selectBrandsAfter(cursorSort, cursorId, size + 1);
        boolean hasMore = records.size() > size;
        if (hasMore) {
            records = records.subList(0, size);
        }
        List<BrandResp> items = brandConverter.toRespList(records);
        Long nextCursor = 0L;
        if (hasMore && !records.isEmpty()) {
            BrandDO last = records.get(records.size() - 1);
            nextCursor = (long) ObjectUtil.defaultIfNull(last.getSort(), 0) * CURSOR_SORT_FACTOR
                                 + last.getId();
        }
        return CursorResult.<BrandResp>builder()
                           .items(items)
                           .cursor(nextCursor)
                           .hasMore(hasMore)
                           .build();
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     * @return 结果标识或数值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, BrandUpdateReq req) {
        BrandDO brand = Asserts.requireNonNull(brandMapper.selectById(id));
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        Asserts.notEmpty(ids, BizErrorEnum.INVALID_PARAM);
        brandMapper.deleteBatchIds(ids);
    }
}
