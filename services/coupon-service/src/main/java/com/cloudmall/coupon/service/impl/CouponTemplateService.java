package com.cloudmall.coupon.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.request.CouponReq;
import com.cloudmall.coupon.api.response.CouponOperationLogResp;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.cache.CouponCacheService;
import com.cloudmall.coupon.convert.CouponConverter;
import com.cloudmall.coupon.convert.CouponOperationLogConverter;
import com.cloudmall.coupon.entity.CouponCategoryDO;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.CouponOperationLogDO;
import com.cloudmall.coupon.entity.CouponProductDO;
import com.cloudmall.coupon.enums.CouponOperationActionEnum;
import com.cloudmall.coupon.enums.CouponScopeEnum;
import com.cloudmall.coupon.enums.CouponStatusEnum;
import com.cloudmall.coupon.mapper.CouponCategoryMapper;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.CouponOperationLogMapper;
import com.cloudmall.coupon.mapper.CouponProductMapper;
import com.cloudmall.coupon.model.req.CouponOperationLogPageReq;
import com.cloudmall.coupon.model.req.CouponPageReq;
import com.cloudmall.coupon.service.ICouponTemplateService;
import com.cloudmall.product.api.client.CategoryClient;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.api.response.ProductResp;

/**
 * 优惠券模板领域服务实现。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateService implements ICouponTemplateService {

    private final CouponMapper couponMapper;
    private final CouponProductMapper couponProductMapper;
    private final CouponCategoryMapper couponCategoryMapper;
    private final CouponOperationLogMapper couponOperationLogMapper;
    private final CouponConverter couponConverter;
    private final CouponOperationLogConverter couponOperationLogConverter;
    private final CouponCacheService couponCacheService;
    private final ProductClient productClient;
    private final CategoryClient categoryClient;

    /**
     * 创建模板草稿。
     *
     * @param req 创建请求
     * @return 新模板 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(CouponReq req) {
        Long shopId = resolveCreateShopId(req);
        validateRule(req, shopId);
        validateScope(req, shopId);

        CouponDO coupon = couponConverter.toDraft(req);
        couponMapper.insert(coupon);
        saveScope(coupon.getId(), req.getScopeType(), req.getProductIds(), req.getCategoryIds());
        appendOperationLog(
                coupon,
                null,
                CouponStatusEnum.DRAFT,
                CouponOperationActionEnum.CREATE,
                "创建模板草稿",
                null
        );
        return coupon.getId();
    }

    /**
     * 编辑模板草稿。
     *
     * @param id 模板 ID
     * @param req 编辑请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long id, CouponReq req) {
        CouponDO coupon = getTemplate(id);
        requireManageable(coupon);
        Asserts.isTrue(coupon.getStatus() == CouponStatusEnum.DRAFT, BizError.COUPON_RULE_FROZEN);
        Asserts.isTrue(req.getOwnerType() == coupon.getOwnerType(), BizError.COUPON_OWNER_INVALID);
        validateRule(req, coupon.getShopId());
        validateScope(req, coupon.getShopId());

        couponConverter.applyUpdate(coupon, req);
        couponMapper.updateById(coupon);
        saveScope(id, req.getScopeType(), req.getProductIds(), req.getCategoryIds());
        appendOperationLog(
                coupon,
                CouponStatusEnum.DRAFT,
                CouponStatusEnum.DRAFT,
                CouponOperationActionEnum.UPDATE,
                "编辑模板草稿",
                null
        );
        couponCacheService.evictPublicTemplates();
    }

    /**
     * 发布模板草稿。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        CouponDO coupon = getTemplate(id);
        requireManageable(coupon);
        Asserts.isTrue(coupon.getStatus() == CouponStatusEnum.DRAFT, BizError.COUPON_OPERATION_NOT_ALLOWED);
        validatePublish(coupon);
        coupon.setStatus(CouponStatusEnum.ACTIVE);
        couponMapper.updateById(coupon);
        appendOperationLog(
                coupon,
                CouponStatusEnum.DRAFT,
                CouponStatusEnum.ACTIVE,
                CouponOperationActionEnum.PUBLISH,
                "发布模板",
                null
        );
        couponCacheService.evictPublicTemplates();
    }

    /**
     * 暂停发放。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(Long id) {
        CouponDO coupon = getTemplate(id);
        requireManageable(coupon);
        Asserts.isTrue(coupon.getStatus() == CouponStatusEnum.ACTIVE, BizError.COUPON_OPERATION_NOT_ALLOWED);
        coupon.setStatus(CouponStatusEnum.PAUSED);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        appendOperationLog(
                coupon,
                CouponStatusEnum.ACTIVE,
                CouponStatusEnum.PAUSED,
                CouponOperationActionEnum.PAUSE,
                "暂停发放",
                null
        );
        couponCacheService.evictPublicTemplates();
    }

    /**
     * 恢复发放。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(Long id) {
        CouponDO coupon = getTemplate(id);
        requireManageable(coupon);
        Asserts.isTrue(coupon.getStatus() == CouponStatusEnum.PAUSED, BizError.COUPON_OPERATION_NOT_ALLOWED);
        coupon.setStatus(CouponStatusEnum.ACTIVE);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        appendOperationLog(
                coupon,
                CouponStatusEnum.PAUSED,
                CouponStatusEnum.ACTIVE,
                CouponOperationActionEnum.RESUME,
                "恢复发放",
                null
        );
        couponCacheService.evictPublicTemplates();
    }

    /**
     * 结束发放。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void end(Long id) {
        CouponDO coupon = getTemplate(id);
        requireManageable(coupon);
        Asserts.isTrue(coupon.isActiveOrPaused(), BizError.COUPON_OPERATION_NOT_ALLOWED);
        CouponStatusEnum from = coupon.getStatus();
        coupon.setStatus(CouponStatusEnum.ENDED);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        appendOperationLog(coupon, from, CouponStatusEnum.ENDED, CouponOperationActionEnum.END, "结束发放", null);
        couponCacheService.evictPublicTemplates();
    }

    /**
     * 删除未发放草稿。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long id) {
        CouponDO coupon = getTemplate(id);
        requireManageable(coupon);
        Asserts.isTrue(coupon.getStatus() == CouponStatusEnum.DRAFT, BizError.COUPON_RULE_FROZEN);
        Long claimed = ObjectUtil.defaultIfNull(coupon.getClaimedCount(), 0L);
        Asserts.isZero(claimed, BizError.COUPON_OPERATION_NOT_ALLOWED);
        couponMapper.deleteById(id);
        deleteScope(id);
        appendOperationLog(
                coupon,
                CouponStatusEnum.DRAFT,
                null,
                CouponOperationActionEnum.DELETE,
                "删除模板草稿",
                null
        );
        couponCacheService.evictPublicTemplates();
    }

    /**
     * 商家分页查询自己店铺的模板。
     *
     * @param req 分页请求
     * @return 模板分页
     */
    @Override
    public IPage<CouponResp> pageShop(CouponPageReq req) {
        Long shopId = Asserts.requireNonNull(UserContextHolder.getShopId(), BizError.MERCHANT_NO_SHOP);
        req.setShopId(shopId);
        IPage<CouponDO> page = couponMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(couponConverter::toResp);
    }

    /**
     * 运营分页查询全部模板。
     *
     * @param req 分页请求
     * @return 模板分页
     */
    @Override
    public IPage<CouponResp> pageAll(CouponPageReq req) {
        Asserts.isTrue(UserContextHolder.isAdmin(), BizError.COUPON_OPERATION_NOT_ALLOWED);
        IPage<CouponDO> page = couponMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(couponConverter::toResp);
    }

    /**
     * 运营分页查询模板操作流水。
     *
     * @param req 分页请求
     * @return 操作流水分页
     */
    @Override
    public IPage<CouponOperationLogResp> pageOperationLogs(CouponOperationLogPageReq req) {
        Asserts.isTrue(UserContextHolder.isAdmin(), BizError.COUPON_OPERATION_NOT_ALLOWED);
        IPage<CouponOperationLogDO> page = couponOperationLogMapper.selectPage(req, req.buildLambdaWrapper());
        return page.convert(couponOperationLogConverter::toResp);
    }

    /**
     * 解析创建模板的店铺 ID：平台券为 null，商家强制当前店铺，运营按请求指定。
     *
     * @param req 创建请求
     * @return 店铺 ID（平台券为 null）
     */
    private Long resolveCreateShopId(CouponReq req) {
        if (req.isPlatform()) {
            return null;
        }
        if (UserContextHolder.isAdmin()) {
            return req.getShopId();
        }
        return UserContextHolder.getShopId();
    }

    /**
     * 查询模板详情（含商品/分类范围）。
     *
     * @param id 模板 ID
     * @return 模板响应
     */
    @Override
    public CouponResp getDetail(Long id) {
        CouponDO coupon = getTemplate(id);
        CouponResp resp = couponConverter.toResp(coupon);
        if (coupon.getScopeType() == CouponScopeEnum.PRODUCT) {
            resp.setProductIds(selectProductIds(id));
        } else if (coupon.getScopeType() == CouponScopeEnum.CATEGORY) {
            resp.setCategoryIds(selectCategoryIds(id));
        }
        return resp;
    }

    /**
     * 查询模板实体。
     *
     * @param id 模板 ID
     * @return 模板实体
     */
    @Override
    public CouponDO getTemplate(Long id) {
        return Asserts.requireNonNull(couponMapper.selectById(id), BizError.COUPON_TEMPLATE_NOT_FOUND);
    }

    /**
     * 校验模板核心规则（创建与编辑共用）。
     *
     * @param req 请求参数
     * @param shopId 店铺 ID
     */
    private void validateRule(CouponReq req, Long shopId) {
        if (req.isRegisterAutoChannel()) {
            Asserts.isTrue(req.isPlatform(), BizError.COUPON_OWNER_INVALID);
        }
        if (req.isShop()) {
            Asserts.notNull(shopId, BizError.COUPON_OWNER_INVALID);
        }
        switch (req.getType()) {
            case FULL_REDUCTION -> {
                Asserts.isTrue(req.isThresholdPositive(), BizError.COUPON_RULE_INVALID);
                Asserts.isTrue(req.isDiscountAmountValid(), BizError.COUPON_RULE_INVALID);
            }
            case CASH -> {
                Asserts.isTrue(req.isThresholdNonNegative(), BizError.COUPON_RULE_INVALID);
                Asserts.isTrue(req.isDiscountAmountPositive(), BizError.COUPON_RULE_INVALID);
            }
            case DISCOUNT -> {
                Asserts.isTrue(req.isThresholdNonNegative(), BizError.COUPON_RULE_INVALID);
                Asserts.isTrue(req.isDiscountRateValid(), BizError.COUPON_RULE_INVALID);
            }
        }
        if (req.isProductScope()) {
            Asserts.isTrue(CollUtil.isNotEmpty(req.getProductIds()), BizError.COUPON_SCOPE_INVALID);
        } else if (req.isCategoryScope()) {
            Asserts.isTrue(CollUtil.isNotEmpty(req.getCategoryIds()), BizError.COUPON_SCOPE_INVALID);
        }
        Asserts.isTrue(req.isIssueTimeValid(), BizError.COUPON_RULE_INVALID);
        if (req.isFixedValidity()) {
            Asserts.isTrue(req.isFixedTimeValid(), BizError.COUPON_RULE_INVALID);
        } else if (req.isRelativeValidity()) {
            Asserts.isTrue(req.hasValidDays(), BizError.COUPON_RULE_INVALID);
        }
    }

    /**
     * 校验适用范围配置（商品/分类存在性，店铺券目标商品必须属于当前店铺）。
     *
     * @param req 请求参数
     * @param shopId 店铺 ID
     */
    private void validateScope(CouponReq req, Long shopId) {
        if (req.isProductScope()) {
            List<Long> distinct = req.getProductIds()
                                     .stream()
                                     .distinct()
                                     .toList();
            for (Long productId : distinct) {
                ProductResp product = callProduct(productId);
                if (req.isShop()) {
                    Asserts.isTrue(shopId.equals(product.getShopId()), BizError.COUPON_SCOPE_INVALID);
                }
            }
        } else if (req.isCategoryScope()) {
            List<Long> distinct = req.getCategoryIds()
                                     .stream()
                                     .distinct()
                                     .toList();
            for (Long categoryId : distinct) {
                callCategory(categoryId);
            }
        }
    }

    /**
     * 发布前完整校验：至少开启一种发放方式，范围配置完整。
     *
     * @param coupon 模板
     */
    private void validatePublish(CouponDO coupon) {
        Asserts.isTrue(coupon.isClaimable(), BizError.COUPON_NOT_CLAIMABLE);
        if (coupon.getScopeType() == CouponScopeEnum.PRODUCT) {
            Long count = couponProductMapper.selectCount(
                    Wrappers.<CouponProductDO>lambdaQuery().eq(CouponProductDO::getCouponId, coupon.getId())
            );
            Asserts.isTrue(count != null && count > 0, BizError.COUPON_SCOPE_INVALID);
        } else if (coupon.getScopeType() == CouponScopeEnum.CATEGORY) {
            Long count = couponCategoryMapper.selectCount(
                    Wrappers.<CouponCategoryDO>lambdaQuery().eq(CouponCategoryDO::getCouponId, coupon.getId())
            );
            Asserts.isTrue(count != null && count > 0, BizError.COUPON_SCOPE_INVALID);
        }
    }

    /**
     * 保存范围关联（先删后插，ALL 范围清空关联）。
     *
     * @param couponId 模板 ID
     * @param scopeType 适用范围
     * @param productIds 商品 ID 列表
     * @param categoryIds 分类 ID 列表
     */
    private void saveScope(
            Long couponId,
            CouponScopeEnum scopeType,
            List<Long> productIds,
            List<Long> categoryIds
    ) {
        if (scopeType == CouponScopeEnum.PRODUCT) {
            couponProductMapper.delete(
                    Wrappers.<CouponProductDO>lambdaQuery().eq(CouponProductDO::getCouponId, couponId)
            );
            List<CouponProductDO> list = productIds
                    .stream()
                    .distinct()
                    .map(
                            productId -> CouponProductDO.builder()
                                                        .couponId(couponId)
                                                        .productId(productId)
                                                        .build()
                    )
                    .toList();
            couponProductMapper.insert(list);
        } else if (scopeType == CouponScopeEnum.CATEGORY) {
            couponCategoryMapper.delete(
                    Wrappers.<CouponCategoryDO>lambdaQuery().eq(CouponCategoryDO::getCouponId, couponId)
            );
            List<CouponCategoryDO> list = categoryIds
                    .stream()
                    .distinct()
                    .map(
                            categoryId -> CouponCategoryDO.builder()
                                                          .couponId(couponId)
                                                          .categoryId(categoryId)
                                                          .build()
                    )
                    .toList();
            couponCategoryMapper.insert(list);
        } else {
            deleteScope(couponId);
        }
    }

    /**
     * 删除模板全部范围关联。
     *
     * @param couponId 模板 ID
     */
    private void deleteScope(Long couponId) {
        couponProductMapper.delete(
                Wrappers.<CouponProductDO>lambdaQuery().eq(CouponProductDO::getCouponId, couponId)
        );
        couponCategoryMapper.delete(
                Wrappers.<CouponCategoryDO>lambdaQuery().eq(CouponCategoryDO::getCouponId, couponId)
        );
    }

    /**
     * 查询模板商品范围。
     *
     * @param couponId 模板 ID
     * @return 商品 ID 列表
     */
    private List<Long> selectProductIds(Long couponId) {
        List<CouponProductDO> list = couponProductMapper.selectList(
                Wrappers.<CouponProductDO>lambdaQuery().eq(CouponProductDO::getCouponId, couponId)
        );
        return list.stream()
                   .map(CouponProductDO::getProductId)
                   .toList();
    }

    /**
     * 查询模板分类范围。
     *
     * @param couponId 模板 ID
     * @return 分类 ID 列表
     */
    private List<Long> selectCategoryIds(Long couponId) {
        List<CouponCategoryDO> list = couponCategoryMapper.selectList(
                Wrappers.<CouponCategoryDO>lambdaQuery().eq(CouponCategoryDO::getCouponId, couponId)
        );
        return list.stream()
                   .map(CouponCategoryDO::getCategoryId)
                   .toList();
    }

    /**
     * 追加模板操作流水。
     *
     * @param coupon 模板
     * @param from 操作前状态
     * @param to 操作后状态
     * @param action 操作类型
     * @param summary 变更摘要
     * @param reason 原因
     */
    private void appendOperationLog(
            CouponDO coupon,
            CouponStatusEnum from,
            CouponStatusEnum to,
            CouponOperationActionEnum action,
            String summary,
            String reason
    ) {
        couponOperationLogMapper.insert(
                couponOperationLogConverter.toLog(coupon, from, to, action, summary, reason)
        );
    }

    /**
     * 调用商品服务校验商品存在性。
     *
     * @param productId 商品 ID
     * @return 商品信息
     */
    private ProductResp callProduct(Long productId) {
        return Asserts.requireNonNull(productClient.getById(productId), BizError.GOODS_NOT_EXIST);
    }

    /**
     * 调用商品服务校验分类存在性。
     *
     * @param categoryId 分类 ID
     * @return 分类信息
     */
    private CategoryResp callCategory(Long categoryId) {
        return Asserts.requireNonNull(
                categoryClient.getById(categoryId),
                BizError.CATEGORY_NOT_EXIST_OR_NO_ENABLE
        );
    }

    /**
     * 校验当前用户可管理该模板：运营放行，非运营必须为店铺拥有者。
     *
     * @param coupon 模板
     */
    private void requireManageable(CouponDO coupon) {
        if (UserContextHolder.isAdmin()) {
            return;
        }
        Long shopId = Asserts.requireNonNull(UserContextHolder.getShopId(), BizError.MERCHANT_NO_SHOP);
        Asserts.isEqual(coupon.getShopId(), shopId, BizError.COUPON_OPERATION_NOT_ALLOWED);
    }
}
