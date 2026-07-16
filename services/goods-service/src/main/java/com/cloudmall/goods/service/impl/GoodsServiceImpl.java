package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.goods.api.request.SearchReq;
import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.entity.GoodsDO;
import com.cloudmall.goods.entity.GoodsSkuDO;
import com.cloudmall.goods.mapper.GoodsMapper;
import com.cloudmall.goods.mapper.GoodsSkuMapper;
import com.cloudmall.goods.service.IGoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements IGoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsSkuMapper goodsSkuMapper;

    @Override
    public GoodsResp getById(Long id) {
        GoodsDO goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return toResponse(goods);
    }

    @Override
    public List<GoodsResp> listByCategory(Long categoryId, int page, int size) {
        List<GoodsDO> list = goodsMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<GoodsDO>()
                        .eq(GoodsDO::getCategoryId, categoryId)
                        .eq(GoodsDO::getStatus, "ON")
        ).getRecords();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<GoodsResp> search(SearchReq request) {
        LambdaQueryWrapper<GoodsDO> wrapper = new LambdaQueryWrapper<GoodsDO>()
                .eq(GoodsDO::getStatus, "ON");
        if (request.getCategoryId() != null) {
            wrapper.eq(GoodsDO::getCategoryId, request.getCategoryId());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(GoodsDO::getName, request.getKeyword());
        }
        // Sort
        if ("price_asc".equals(request.getSortBy())) {
            wrapper.orderByAsc(GoodsDO::getPrice);
        } else if ("price_desc".equals(request.getSortBy())) {
            wrapper.orderByDesc(GoodsDO::getPrice);
        } else if ("sales".equals(request.getSortBy())) {
            wrapper.orderByDesc(GoodsDO::getSalesCount);
        } else {
            wrapper.orderByDesc(GoodsDO::getCreateTime);
        }

        List<GoodsDO> list = goodsMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper
        ).getRecords();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean deductStock(Long skuId, Integer quantity) {
        GoodsSkuDO sku = goodsSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        if (sku.getStock() < quantity) {
            throw new BizException(BizErrorCode.STOCK_NOT_ENOUGH);
        }
        sku.setStock(sku.getStock() - quantity);
        goodsSkuMapper.updateById(sku);
        return true;
    }

    @Override
    @Transactional
    public Boolean rollbackStock(Long skuId, Integer quantity) {
        GoodsSkuDO sku = goodsSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        sku.setStock(sku.getStock() + quantity);
        goodsSkuMapper.updateById(sku);
        return true;
    }

    private GoodsResp toResponse(GoodsDO goods) {
        GoodsResp r = new GoodsResp();
        r.setId(goods.getId());
        r.setName(goods.getName());
        r.setImage(goods.getImage());
        r.setPrice(goods.getPrice());
        r.setCategoryId(goods.getCategoryId());
        r.setSalesCount(goods.getSalesCount());
        r.setStock(goods.getStock());
        r.setStatus(goods.getStatus());
        r.setDescription(goods.getDescription());
        return r;
    }
}
