package com.cloudmall.seckill.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.model.resp.ProductResp;
import com.cloudmall.product.api.model.resp.SkuResp;
import com.cloudmall.seckill.api.model.resp.SeckillProductResp;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.enums.AuditBizTypeEnum;
import com.cloudmall.seckill.enums.AuditStatusEnum;
import com.cloudmall.seckill.enums.SeckillProductAuditStatusEnum;
import com.cloudmall.seckill.mapper.AuditRecordMapper;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.model.entity.AuditRecordDO;
import com.cloudmall.seckill.model.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.entity.SeckillProductDO;
import com.cloudmall.seckill.model.req.SeckillProductPageReq;
import com.cloudmall.seckill.service.ISeckillProductService;

/**
 * 秒杀商品与审核服务实现
 * <p>
 * 提交时写入 cm_seckill_product（PENDING）与 cm_audit_record（PENDING）同事务；
 * 审核时更新审核单并同步商品审核状态。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillProductService implements ISeckillProductService {

    /** 每个活动最多可报名的商品数（活动未配置时的兜底默认值） */
    private static final int DEFAULT_MAX_PRODUCTS_PER_ACTIVITY = 50;

    private final SeckillProductMapper productMapper;
    private final SeckillActivityMapper activityMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final SeckillConverter seckillConverter;
    private final ProductClient productClient;
    private final SkuClient skuClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(SeckillProductDO product, Long submitterId) {
        SeckillActivityDO activity = Asserts.requireNonNull(
                activityMapper.selectById(product.getActivityId()),
                BizErrorEnum.SECKILL_ACTIVITY_NOT_EXIST
        );

        // 仅 PUBLISHED 且未开始（报名截止 = start_time）的活动可提交
        Asserts.isTrue(activity.isPublished(), BizErrorEnum.SECKILL_PRODUCT_NOT_SUBMITTABLE);
        Asserts.isTrue(
                activity.isNotStarted(LocalDateTime.now()),
                BizErrorEnum.SECKILL_PRODUCT_NOT_SUBMITTABLE
        );

        // 同活动同 SKU 唯一
        Long existing = productMapper.countByActivityAndSku(product.getActivityId(), product.getSkuId());
        Asserts.isTrue(existing == null || existing == 0, BizErrorEnum.PRODUCT_ALREADY_IN_SECKILL_ACTIVITY);

        // 每活动商品数上限（创建活动时配置，未配置兜底 50）
        Long count = productMapper.countByActivityId(product.getActivityId());
        int maxProducts = ObjectUtil.defaultIfNull(
                activity.getMaxProducts(),
                DEFAULT_MAX_PRODUCTS_PER_ACTIVITY
        );
        Asserts.isTrue(count == null || count < maxProducts, BizErrorEnum.ACTIVITY_MAX_ITEMS_REACHED);

        product.setAuditStatus(SeckillProductAuditStatusEnum.PENDING.getCode());
        product.setSoldCount(0);
        productMapper.insert(product);

        auditRecordMapper.insert(
                AuditRecordDO.builder()
                             .bizType(AuditBizTypeEnum.SECKILL_PRODUCT.getCode())
                             .bizId(product.getId())
                             .submitterId(submitterId)
                             .status(AuditStatusEnum.PENDING.getCode())
                             .extraInfo(JSONUtil.toJsonStr(product))
                             .build()
        );
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long productId, boolean approved, String auditRemark, Long auditorId) {
        SeckillProductDO product = Asserts.requireNonNull(
                productMapper.selectById(productId),
                BizErrorEnum.SECKILL_PRODUCT_NOT_FOUND
        );

        // 仅待审核的商品可审核
        Asserts.isTrue(
                SeckillProductAuditStatusEnum.PENDING.getCode().equals(product.getAuditStatus()),
                BizErrorEnum.AUDIT_TARGET_STATUS_CONFLICT
        );

        AuditRecordDO record = Asserts.requireNonNull(
                auditRecordMapper.selectPendingByBiz(AuditBizTypeEnum.SECKILL_PRODUCT.getCode(), productId),
                BizErrorEnum.AUDIT_NOT_EXIST
        );

        record.setStatus(approved ? AuditStatusEnum.APPROVED.getCode() : AuditStatusEnum.REJECTED.getCode());
        record.setAuditRemark(auditRemark);
        record.setAuditorId(auditorId);
        auditRecordMapper.updateById(record);

        product.setAuditStatus(
                approved
                        ? SeckillProductAuditStatusEnum.APPROVED.getCode()
                        : SeckillProductAuditStatusEnum.REJECTED.getCode()
        );
        productMapper.updateById(product);
    }

    /**
     * 分页查询秒杀商品（运营端按审核状态筛选）。
     *
     * @param req 分页请求
     * @return 商品分页
     */
    @Override
    public IPage<SeckillProductResp> pageProducts(SeckillProductPageReq req) {
        IPage<SeckillProductDO> page = productMapper.selectProductPage(req);
        IPage<SeckillProductResp> result = page.convert(seckillConverter::toSeckillProductResp);
        enrich(result.getRecords());
        return result;
    }

    /**
     * 查询活动中某 SKU 已审核通过的商品（抢购编排用）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @return 已审核通过的商品，不存在时返回 null
     */
    @Override
    public SeckillProductDO getApprovedByActivityAndSku(Long activityId, Long skuId) {
        return productMapper.selectApprovedByActivityAndSku(activityId, skuId);
    }

    /**
     * 按秒杀商品 ID 查询已审核通过的商品（用户端详情用）。
     *
     * @param id 秒杀商品 ID
     * @return 秒杀商品响应
     */
    @Override
    public SeckillProductResp getApprovedById(Long id) {
        SeckillProductDO product = Asserts.requireNonNull(
                productMapper.selectApprovedById(id),
                BizErrorEnum.SECKILL_PRODUCT_NOT_FOUND
        );
        SeckillProductResp resp = seckillConverter.toSeckillProductResp(product);
        enrich(List.of(resp));
        return resp;
    }

    /**
     * 查询店铺提交的全部商品（商家端“我的提交”）。
     *
     * @param shopId 店铺 ID
     * @return 商品列表
     */
    @Override
    public List<SeckillProductResp> listByShopId(Long shopId) {
        List<SeckillProductDO> products = productMapper.listByShopId(shopId);
        List<SeckillProductResp> result = seckillConverter.toSeckillProductRespList(products);
        enrich(result);
        return result;
    }

    /**
     * 为秒杀商品响应批量补充商品名称、主图与划线价。
     *
     * @param products 秒杀商品响应列表
     */
    @Override
    public void enrich(List<SeckillProductResp> products) {
        if (CollUtil.isEmpty(products)) {
            return;
        }
        enrichProductInfo(products);
        enrichOriginalPrice(products);
    }

    /**
     * 批量补充商品名称与主图。
     *
     * @param products 秒杀商品响应列表
     */
    private void enrichProductInfo(List<SeckillProductResp> products) {
        List<Long> productIds = products.stream()
                                        .map(SeckillProductResp::getProductId)
                                        .filter(ObjectUtil::isNotNull)
                                        .distinct()
                                        .toList();
        if (CollUtil.isEmpty(productIds)) {
            return;
        }
        try {
            Map<Long, ProductResp> productMap = productClient
                    .listByIds(productIds)
                    .stream()
                    .collect(Collectors.toMap(ProductResp::getId, p -> p, (left, right) -> left));
            for (SeckillProductResp product : products) {
                ProductResp detail = productMap.get(product.getProductId());
                if (detail != null) {
                    product.setProductName(detail.getName());
                    product.setProductDescription(detail.getDescription());
                    product.setImage(detail.getImage());
                }
            }
        } catch (Exception e) {
            log.warn("补充秒杀商品展示信息失败，productIds={}", productIds, e);
        }
    }

    /**
     * 按秒杀 SKU 批量补充商品原价（市场价优先，缺失时回退销售价）。
     *
     * @param products 秒杀商品响应列表
     */
    private void enrichOriginalPrice(List<SeckillProductResp> products) {
        List<Long> skuIds = products.stream()
                                    .map(SeckillProductResp::getSkuId)
                                    .filter(ObjectUtil::isNotNull)
                                    .distinct()
                                    .toList();
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        try {
            List<SkuResp> skus = skuClient.listSkusByIds(skuIds);
            Map<Long, SkuResp> skuMap = CollUtil
                    .emptyIfNull(skus)
                    .stream()
                    .collect(Collectors.toMap(SkuResp::getSkuId, sku -> sku, (left, right) -> left));
            for (SeckillProductResp product : products) {
                SkuResp sku = skuMap.get(product.getSkuId());
                if (sku != null) {
                    product.setOriginalPrice(resolveOriginalPrice(sku));
                }
            }
        } catch (Exception e) {
            log.warn("补充秒杀商品原价失败，skuIds={}", skuIds, e);
        }
    }

    /**
     * 解析秒杀商品划线价：优先使用市场价，市场价缺失时回退销售价。
     *
     * @param sku SKU 信息
     * @return 划线价，价格均缺失时返回 null
     */
    private BigDecimal resolveOriginalPrice(SkuResp sku) {
        BigDecimal marketPrice = sku.getMarketPrice();
        BigDecimal price = sku.getPrice();
        if (marketPrice == null) {
            return price;
        }
        if (price == null) {
            return marketPrice;
        }
        return marketPrice.max(price);
    }

    /**
     * 校验活动中某商品是否已审核通过（内部校验用）。
     *
     * @param activityId 活动 ID
     * @param productId 商品 ID
     * @return 已审核通过返回 true
     */
    @Override
    public boolean isApproved(Long activityId, Long productId) {
        return productMapper.selectApprovedProduct(activityId, productId) != null;
    }
}
