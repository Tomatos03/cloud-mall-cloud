package com.cloudmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.product.entity.ProductDO;
import com.cloudmall.product.entity.ProductSkuDO;
import com.cloudmall.product.convert.ProductConverter;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.product.mapper.ProductSkuMapper;
import com.cloudmall.product.model.req.ProductGetReq;
import com.cloudmall.product.model.req.ProductPageReq;
import com.cloudmall.product.service.IProductService;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductConverter productConverter;

    @Override
    public ProductResp get(ProductGetReq req) {
        LambdaQueryWrapper<ProductDO> wrapper = req.buildLambdaWrapper();
        ProductDO product = productMapper.selectOne(wrapper);
        return productConverter.toResp(product);
    }

    @Override
    public IPage<ProductResp> page(ProductPageReq req) {
        LambdaQueryWrapper<ProductDO> wrapper = req.buildLambdaWrapper();
        wrapper.eq(ProductDO::getStatus, StatusEnum.ENABLED);
        wrapper.orderByDesc(ProductDO::getCreateTime);
        return productMapper.selectPage(req, wrapper)
                          .convert(productConverter::toResp);
    }

    @Override
    @Transactional
    public Boolean deductStock(Long skuId, Integer quantity) {
        ProductSkuDO sku = productSkuMapper.selectById(skuId);
        AssertUtils.notNull(sku, BizErrorCode.DATA_NOT_FOUND);
        AssertUtils.greaterOrEqual(sku.getStock(), quantity, BizErrorCode.STOCK_NOT_ENOUGH);
        sku.setStock(sku.getStock() - quantity);
        productSkuMapper.updateById(sku);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean rollbackStock(Long skuId, Integer quantity) {
        ProductSkuDO sku = productSkuMapper.selectById(skuId);
        AssertUtils.notNull(sku, BizErrorCode.DATA_NOT_FOUND);
        sku.setStock(sku.getStock() + quantity);
        productSkuMapper.updateById(sku);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean updateStock(Long skuId, Integer quantity) {
        if (quantity >= 0) {
            return rollbackStock(skuId, quantity);
        } else {
            return deductStock(skuId, -quantity);
        }
    }

}
