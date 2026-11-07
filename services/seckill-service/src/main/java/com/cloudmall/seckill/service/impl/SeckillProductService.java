package com.cloudmall.seckill.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.seckill.api.model.resp.SeckillProductResp;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.model.entity.AuditRecordDO;
import com.cloudmall.seckill.model.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.entity.SeckillProductDO;
import com.cloudmall.seckill.enums.AuditBizTypeEnum;
import com.cloudmall.seckill.enums.AuditStatusEnum;
import com.cloudmall.seckill.enums.SeckillProductAuditStatusEnum;
import com.cloudmall.seckill.mapper.AuditRecordMapper;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
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
@Service
@RequiredArgsConstructor
public class SeckillProductService implements ISeckillProductService {

    /** 每个活动最多可报名的商品数（活动未配置时的兜底默认值） */
    private static final int DEFAULT_MAX_PRODUCTS_PER_ACTIVITY = 50;

    private final SeckillProductMapper productMapper;
    private final SeckillActivityMapper activityMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final SeckillConverter seckillConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(SeckillProductDO product, Long submitterId) {
        SeckillActivityDO activity = Asserts.requireNonNull(
                activityMapper.selectById(product.getActivityId()),
                BizErrorEnum.SECKILL_ACTIVITY_NOT_EXIST
        );

        // 仅 PUBLISHED 且未开始（报名截止 = start_time）的活动可提交
        Asserts.isTrue(activity.isPublished(), BizErrorEnum.SECKILL_PRODUCT_NOT_SUBMITTABLE);
        Asserts.isTrue(activity.isNotStarted(LocalDateTime.now()), BizErrorEnum.SECKILL_PRODUCT_NOT_SUBMITTABLE);

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
        return page.convert(seckillConverter::toSeckillProductResp);
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
     * 查询店铺提交的全部商品（商家端“我的提交”）。
     *
     * @param shopId 店铺 ID
     * @return 商品列表
     */
    @Override
    public List<SeckillProductResp> listByShopId(Long shopId) {
        List<SeckillProductDO> products = productMapper.listByShopId(shopId);
        return seckillConverter.toSeckillProductRespList(products);
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
