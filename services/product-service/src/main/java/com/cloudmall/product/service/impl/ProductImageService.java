package com.cloudmall.product.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.product.api.response.ImageResp;
import com.cloudmall.product.convert.ImageConverter;
import com.cloudmall.product.entity.ProductImageDO;
import com.cloudmall.product.mapper.ProductImageMapper;
import com.cloudmall.product.service.IProductImageService;

/**
 * 商品图片服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService {

    private final ProductImageMapper productImageMapper;
    private final ImageConverter imageConverter;

    /**
     * 查询数据列表。
     *
     * @param productId 商品标识
     * @return 数据列表
     */
    @Override
    public List<ImageResp> listByProductId(Long productId) {
        List<ProductImageDO> images = productImageMapper.selectList(
                Wrappers.<ProductImageDO>lambdaQuery()
                        .eq(ProductImageDO::getProductId, productId)
                        .orderByDesc(ProductImageDO::getIsMain)
                        .orderByAsc(ProductImageDO::getSortOrder)
        );
        return imageConverter.toRespList(images);
    }
}
