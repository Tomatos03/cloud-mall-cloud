package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.goods.api.response.GoodsResp;
import com.cloudmall.goods.entity.GoodsDO;
import com.cloudmall.goods.entity.GoodsSkuDO;
import com.cloudmall.goods.convert.GoodsConverter;
import com.cloudmall.goods.mapper.GoodsMapper;
import com.cloudmall.goods.mapper.GoodsSkuMapper;
import com.cloudmall.goods.model.req.GoodsPageReq;
import com.cloudmall.goods.service.IGoodsService;

@Service
@RequiredArgsConstructor
public class GoodsService implements IGoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsConverter goodsConverter;

    @Override
    public GoodsResp getById(Long id) {
        GoodsDO goods = goodsMapper.selectById(id);
        AssertUtils.notNull(goods, BizErrorCode.DATA_NOT_FOUND);
        return goodsConverter.toResp(goods);
    }

    @Override
    public GoodsResp getBySpu(String spu) {
        GoodsDO goods = goodsMapper.selectOne(
                Wrappers.<GoodsDO>lambdaQuery()
                        .eq(GoodsDO::getSpu, spu)
                        .eq(GoodsDO::getStatus, StatusEnum.ENABLED)
        );
        return goods == null ? null : goodsConverter.toResp(goods);
    }

    @Override
    public IPage<GoodsResp> page(GoodsPageReq req) {
        LambdaQueryWrapper<GoodsDO> wrapper = req.buildLambdaWrapper();
        wrapper.eq(GoodsDO::getStatus, StatusEnum.ENABLED);
        wrapper.orderByDesc(GoodsDO::getCreateTime);
        return goodsMapper.selectPage(req, wrapper)
                          .convert(goodsConverter::toResp);
    }

    @Override
    @Transactional
    public Boolean deductStock(Long skuId, Integer quantity) {
        GoodsSkuDO sku = goodsSkuMapper.selectById(skuId);
        AssertUtils.notNull(sku, BizErrorCode.DATA_NOT_FOUND);
        AssertUtils.greaterOrEqual(sku.getStock(), quantity, BizErrorCode.STOCK_NOT_ENOUGH);
        sku.setStock(sku.getStock() - quantity);
        goodsSkuMapper.updateById(sku);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean rollbackStock(Long skuId, Integer quantity) {
        GoodsSkuDO sku = goodsSkuMapper.selectById(skuId);
        AssertUtils.notNull(sku, BizErrorCode.DATA_NOT_FOUND);
        sku.setStock(sku.getStock() + quantity);
        goodsSkuMapper.updateById(sku);
        return Boolean.TRUE;
    }

}
