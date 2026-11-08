package com.cloudmall.product.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.model.resp.ProductQaResp;
import com.cloudmall.product.convert.ProductQaConverter;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.product.mapper.ProductQaMapper;
import com.cloudmall.product.model.entity.ProductQaDO;
import com.cloudmall.product.service.IProductQaService;

/**
 * 商品问答查询服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class ProductQaService implements IProductQaService {

    /** 默认返回条数 */
    private static final int DEFAULT_LIMIT = 5;

    /** 返回条数上限 */
    private static final int MAX_LIMIT = 20;

    private final ProductQaMapper productQaMapper;
    private final ProductMapper productMapper;
    private final ProductQaConverter productQaConverter;

    /**
     * 查询商品问答列表。
     *
     * @param productId 商品 SPU ID
     * @param limit 返回条数上限
     * @return 问答列表
     */
    @Override
    public List<ProductQaResp> listByProduct(Long productId, Integer limit) {
        Asserts.notNull(productId, BizErrorEnum.PRODUCT_ID_INVALID);
        Asserts.requireNonNull(productMapper.selectById(productId), BizErrorEnum.PRODUCT_DETAIL_NOT_FOUND);

        int size = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        List<ProductQaDO> qas = productQaMapper.listVisibleByProduct(productId, size);
        return productQaConverter.toRespList(qas);
    }
}
