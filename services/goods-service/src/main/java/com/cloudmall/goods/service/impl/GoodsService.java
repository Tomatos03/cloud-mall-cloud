package com.cloudmall.goods.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.goods.api.request.SearchReq;
import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.entity.GoodsDO;
import com.cloudmall.goods.entity.GoodsSkuDO;
import com.cloudmall.goods.mapper.GoodsMapper;
import com.cloudmall.goods.mapper.GoodsSkuMapper;
import com.cloudmall.goods.service.IGoodsService;

@Service
@RequiredArgsConstructor
public class GoodsService implements IGoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsSkuMapper goodsSkuMapper;

    @Override
    public GoodsResp getById(Long id) {
        GoodsDO goods = goodsMapper.selectById(id);
        AssertUtils.notNull(goods, BizErrorCode.DATA_NOT_FOUND);
        return toResponse(goods);
    }

    @Override
    public List<GoodsResp> listByCategory(Long categoryId, int page, int size) {
        List<GoodsDO> list = goodsMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<GoodsDO>lambdaQuery()
                        .eq(GoodsDO::getCategoryId, categoryId)
                        .eq(GoodsDO::getStatus, "ON")
        ).getRecords();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<GoodsResp> search(SearchReq request) {
        var wrapper = Wrappers.<GoodsDO>lambdaQuery()
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
        AssertUtils.notNull(sku, BizErrorCode.DATA_NOT_FOUND);
        AssertUtils.greaterOrEqual(sku.getStock(), quantity, BizErrorCode.STOCK_NOT_ENOUGH);
        sku.setStock(sku.getStock() - quantity);
        goodsSkuMapper.updateById(sku);
        return true;
    }

    @Override
    @Transactional
    public Boolean rollbackStock(Long skuId, Integer quantity) {
        GoodsSkuDO sku = goodsSkuMapper.selectById(skuId);
        AssertUtils.notNull(sku, BizErrorCode.DATA_NOT_FOUND);
        sku.setStock(sku.getStock() + quantity);
        goodsSkuMapper.updateById(sku);
        return true;
    }

    private GoodsResp toResponse(GoodsDO goods) {
        return GoodsResp.builder()
                .id(goods.getId())
                .name(goods.getName())
                .image(goods.getImage())
                .price(goods.getPrice())
                .categoryId(goods.getCategoryId())
                .salesCount(goods.getSalesCount())
                .stock(goods.getStock())
                .status(goods.getStatus())
                .description(goods.getDescription())
                .build();
    }
}
