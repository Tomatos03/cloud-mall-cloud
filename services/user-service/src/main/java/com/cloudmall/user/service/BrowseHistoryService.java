package com.cloudmall.user.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.model.resp.ImageResp;
import com.cloudmall.product.api.model.resp.ProductDetailResp;
import com.cloudmall.product.api.model.resp.ProductSku;
import com.cloudmall.user.api.model.resp.BrowseHistoryResp;
import com.cloudmall.user.convert.UserProfileConverter;
import com.cloudmall.user.mapper.BrowseHistoryMapper;
import com.cloudmall.user.model.entity.BrowseHistoryDO;

/**
 * 用户商品浏览记录服务。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class BrowseHistoryService {

    private final BrowseHistoryMapper browseHistoryMapper;
    private final ProductClient productClient;
    private final UserProfileConverter userProfileConverter;
    private final UserPointService userPointService;

    /**
     * 查询当前用户浏览记录。
     *
     * @param userId 用户 ID
     * @return 浏览记录
     */
    public List<BrowseHistoryResp> list(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        return userProfileConverter.toBrowseHistoryRespList(browseHistoryMapper.listByUserId(userId));
    }

    /**
     * 记录一次商品浏览，同一商品只保留最新记录。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @return 浏览记录
     */
    @Transactional(rollbackFor = Exception.class)
    public BrowseHistoryResp record(Long userId, Long productId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        Asserts.notNull(productId, BizErrorEnum.PRODUCT_ID_INVALID);
        ProductDetailResp product = productClient.detail(productId);
        Asserts.requireNonNull(product, BizErrorEnum.PRODUCT_NOT_EXIST);

        LocalDateTime now = LocalDateTime.now();
        BrowseHistoryDO history = browseHistoryMapper.selectByUserAndProduct(userId, productId);
        if (history == null) {
            history = BrowseHistoryDO.builder()
                                     .userId(userId)
                                     .productId(productId)
                                     .browseTime(now)
                                     .build();
            fillProduct(history, product);
            browseHistoryMapper.insert(history);
        } else {
            fillProduct(history, product);
            history.setBrowseTime(now);
            browseHistoryMapper.updateById(history);
        }
        userPointService.completeTask(userId, "DAILY_BROWSE");
        return userProfileConverter.toBrowseHistoryResp(history);
    }

    /**
     * 删除单条浏览记录。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     */
    public void remove(Long userId, Long productId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        browseHistoryMapper.deleteByUserAndProduct(userId, productId);
    }

    /**
     * 清空当前用户浏览记录。
     *
     * @param userId 用户 ID
     */
    public void clear(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        browseHistoryMapper.deleteByUserId(userId);
    }

    /**
     * 将商品详情快照写入浏览记录。
     *
     * @param history 浏览记录
     * @param product 商品详情
     */
    private void fillProduct(BrowseHistoryDO history, ProductDetailResp product) {
        history.setProductName(product.getName());
        history.setProductImage(resolveImage(product));
        history.setPrice(resolvePrice(product));
    }

    /**
     * 解析商品主图。
     *
     * @param product 商品详情
     * @return 图片地址
     */
    private String resolveImage(ProductDetailResp product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages()
                      .stream()
                      .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                      .map(ImageResp::getUrl)
                      .findFirst()
                      .orElseGet(() -> product.getImages()
                                              .get(0)
                                              .getUrl());
    }

    /**
     * 解析商品最低售价。
     *
     * @param product 商品详情
     * @return 最低售价
     */
    private BigDecimal resolvePrice(ProductDetailResp product) {
        if (product.getSkus() == null || product.getSkus().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return product.getSkus()
                      .stream()
                      .map(ProductSku::getPrice)
                      .filter(Objects::nonNull)
                      .min(BigDecimal::compareTo)
                      .orElse(BigDecimal.ZERO);
    }

}
