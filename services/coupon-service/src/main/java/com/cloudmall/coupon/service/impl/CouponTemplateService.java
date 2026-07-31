package com.cloudmall.coupon.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.model.Result;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.coupon.api.request.CouponBaseReq;
import com.cloudmall.coupon.api.request.CouponCreateReq;
import com.cloudmall.coupon.api.request.CouponUpdateReq;
import com.cloudmall.coupon.api.response.CouponResp;
import com.cloudmall.coupon.convert.CouponConverter;
import com.cloudmall.coupon.entity.CouponCategoryDO;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.CouponOperationLogDO;
import com.cloudmall.coupon.entity.CouponProductDO;
import com.cloudmall.coupon.enums.CouponOperationActionEnum;
import com.cloudmall.coupon.enums.CouponOperatorTypeEnum;
import com.cloudmall.coupon.enums.CouponOwnerTypeEnum;
import com.cloudmall.coupon.enums.CouponScopeTypeEnum;
import com.cloudmall.coupon.enums.CouponStatusEnum;
import com.cloudmall.coupon.enums.CouponTypeEnum;
import com.cloudmall.coupon.enums.ValidityModeEnum;
import com.cloudmall.coupon.mapper.CouponCategoryMapper;
import com.cloudmall.coupon.mapper.CouponMapper;
import com.cloudmall.coupon.mapper.CouponOperationLogMapper;
import com.cloudmall.coupon.mapper.CouponProductMapper;
import com.cloudmall.coupon.service.ICouponTemplateService;
import com.cloudmall.product.api.client.CategoryClient;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.api.response.ProductResp;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.response.ShopResp;

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
    private final ProductClient productClient;
    private final CategoryClient categoryClient;
    private final ShopClient shopClient;

    /**
     * 创建模板草稿。
     *
     * @param req 创建请求
     * @return 新模板 ID
     */
    @Override
    @Transactional
    public Long createDraft(CouponCreateReq req) {
        CouponOwnerTypeEnum ownerType = CouponOwnerTypeEnum.fromCode(req.getOwnerType());
        CouponTypeEnum type = CouponTypeEnum.fromCode(req.getType());
        CouponScopeTypeEnum scopeType = CouponScopeTypeEnum.fromCode(req.getScopeType());
        ValidityModeEnum validityMode = ValidityModeEnum.fromCode(req.getValidityMode());
        Long shopId = resolveShopId(ownerType);
        validateRule(req, ownerType, type, scopeType, validityMode, shopId);
        validateScope(scopeType, req.getProductIds(), req.getCategoryIds(), ownerType, shopId);

        LocalDateTime now = LocalDateTime.now();
        CouponDO coupon = CouponDO
                .builder()
                .name(req.getName())
                .ownerType(ownerType)
                .shopId(shopId)
                .createdBy(currentUserId())
                .type(type)
                .scopeType(scopeType)
                .threshold(ObjectUtil.defaultIfNull(req.getThreshold(), BigDecimal.ZERO))
                .discountAmount(req.getDiscountAmount())
                .discountRate(req.getDiscountRate())
                .totalCount(req.getTotalCount())
                .claimedCount(0L)
                .claimEnabled(BooleanUtil.isTrue(req.getClaimEnabled()))
                .autoIssueOnRegister(BooleanUtil.isTrue(req.getAutoIssueOnRegister()))
                .validityMode(validityMode)
                .issueStartTime(req.getIssueStartTime())
                .issueEndTime(req.getIssueEndTime())
                .fixedStartTime(req.getFixedStartTime())
                .fixedExpireTime(req.getFixedExpireTime())
                .validDays(req.getValidDays())
                .status(CouponStatusEnum.DRAFT)
                .firstIssuedTime(null)
                .createTime(now)
                .updateTime(now)
                .deleted(false)
                .build();
        couponMapper.insert(coupon);
        saveScope(coupon.getId(), scopeType, req.getProductIds(), req.getCategoryIds(), now);
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
     * @param id  模板 ID
     * @param req 编辑请求
     */
    @Override
    @Transactional
    public void updateDraft(Long id, CouponUpdateReq req) {
        CouponDO coupon = getTemplate(id);
        AssertUtils.isTrue(coupon.getStatus() == CouponStatusEnum.DRAFT, BizErrorCode.COUPON_RULE_FROZEN);
        CouponTypeEnum type = CouponTypeEnum.fromCode(req.getType());
        CouponScopeTypeEnum scopeType = CouponScopeTypeEnum.fromCode(req.getScopeType());
        ValidityModeEnum validityMode = ValidityModeEnum.fromCode(req.getValidityMode());
        validateRule(
                req,
                coupon.getOwnerType(),
                type,
                scopeType,
                validityMode,
                coupon.getShopId()
        );
        validateScope(
                scopeType,
                req.getProductIds(),
                req.getCategoryIds(),
                coupon.getOwnerType(),
                coupon.getShopId()
        );

        LocalDateTime now = LocalDateTime.now();
        coupon.setName(req.getName());
        coupon.setType(type);
        coupon.setScopeType(scopeType);
        coupon.setThreshold(ObjectUtil.defaultIfNull(req.getThreshold(), BigDecimal.ZERO));
        coupon.setDiscountAmount(req.getDiscountAmount());
        coupon.setDiscountRate(req.getDiscountRate());
        coupon.setTotalCount(req.getTotalCount());
        coupon.setClaimEnabled(BooleanUtil.isTrue(req.getClaimEnabled()));
        coupon.setAutoIssueOnRegister(BooleanUtil.isTrue(req.getAutoIssueOnRegister()));
        coupon.setValidityMode(validityMode);
        coupon.setIssueStartTime(req.getIssueStartTime());
        coupon.setIssueEndTime(req.getIssueEndTime());
        coupon.setFixedStartTime(req.getFixedStartTime());
        coupon.setFixedExpireTime(req.getFixedExpireTime());
        coupon.setValidDays(req.getValidDays());
        coupon.setUpdateTime(now);
        couponMapper.updateById(coupon);
        saveScope(id, scopeType, req.getProductIds(), req.getCategoryIds(), now);
        appendOperationLog(
                coupon,
                CouponStatusEnum.DRAFT,
                CouponStatusEnum.DRAFT,
                CouponOperationActionEnum.UPDATE,
                "编辑模板草稿",
                null
        );
    }

    /**
     * 发布模板草稿。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional
    public void publish(Long id) {
        CouponDO coupon = getTemplate(id);
        AssertUtils.isTrue(
                coupon.getStatus() == CouponStatusEnum.DRAFT,
                BizErrorCode.COUPON_OPERATION_NOT_ALLOWED
        );
        validatePublish(coupon);
        coupon.setStatus(CouponStatusEnum.ACTIVE);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        appendOperationLog(
                coupon,
                CouponStatusEnum.DRAFT,
                CouponStatusEnum.ACTIVE,
                CouponOperationActionEnum.PUBLISH,
                "发布模板",
                null
        );
    }

    /**
     * 暂停发放。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional
    public void pause(Long id) {
        CouponDO coupon = getTemplate(id);
        AssertUtils.isTrue(
                coupon.getStatus() == CouponStatusEnum.ACTIVE,
                BizErrorCode.COUPON_OPERATION_NOT_ALLOWED
        );
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
    }

    /**
     * 恢复发放。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional
    public void resume(Long id) {
        CouponDO coupon = getTemplate(id);
        AssertUtils.isTrue(
                coupon.getStatus() == CouponStatusEnum.PAUSED,
                BizErrorCode.COUPON_OPERATION_NOT_ALLOWED
        );
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
    }

    /**
     * 结束发放。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional
    public void end(Long id) {
        CouponDO coupon = getTemplate(id);
        AssertUtils.isTrue(coupon.isActiveOrPaused(), BizErrorCode.COUPON_OPERATION_NOT_ALLOWED);
        CouponStatusEnum from = coupon.getStatus();
        coupon.setStatus(CouponStatusEnum.ENDED);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        appendOperationLog(coupon, from, CouponStatusEnum.ENDED, CouponOperationActionEnum.END, "结束发放", null);
    }

    /**
     * 删除未发放草稿。
     *
     * @param id 模板 ID
     */
    @Override
    @Transactional
    public void deleteDraft(Long id) {
        CouponDO coupon = getTemplate(id);
        AssertUtils.isTrue(coupon.getStatus() == CouponStatusEnum.DRAFT, BizErrorCode.COUPON_RULE_FROZEN);
        Long claimed = ObjectUtil.defaultIfNull(coupon.getClaimedCount(), 0L);
        AssertUtils.isZero(claimed, BizErrorCode.COUPON_OPERATION_NOT_ALLOWED);
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
        if (coupon.getScopeType() == CouponScopeTypeEnum.PRODUCT) {
            resp.setProductIds(selectProductIds(id));
        } else if (coupon.getScopeType() == CouponScopeTypeEnum.CATEGORY) {
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
        CouponDO coupon = couponMapper.selectById(id);
        AssertUtils.notNull(coupon, BizErrorCode.COUPON_TEMPLATE_NOT_FOUND);
        return coupon;
    }

    /**
     * 解析店铺归属：SHOP 归属时从当前用户上下文查询唯一店铺。
     *
     * @param ownerType 券归属
     * @return 店铺 ID（平台券为 null）
     */
    private Long resolveShopId(CouponOwnerTypeEnum ownerType) {
        if (ownerType == CouponOwnerTypeEnum.PLATFORM) {
            return null;
        }
        Result<ShopResp> result = shopClient.getByUserId(currentUserId());
        AssertUtils.isTrue(
                result.getCode() == Result.SUCCESS_CODE && result.getData() != null,
                BizErrorCode.MERCHANT_NO_SHOP
        );
        return result.getData().getId();
    }

    /**
     * 校验模板核心规则（创建与编辑共用）。
     *
     * @param req          请求参数
     * @param ownerType    券归属
     * @param type         券类型
     * @param scopeType    适用范围
     * @param validityMode 有效期模式
     * @param shopId       店铺 ID
     */
    private void validateRule(
            CouponBaseReq req,
            CouponOwnerTypeEnum ownerType,
            CouponTypeEnum type,
            CouponScopeTypeEnum scopeType,
            ValidityModeEnum validityMode,
            Long shopId
    ) {
        AssertUtils.isFalse(req.isBothIssueChannelsEnabled(), BizErrorCode.COUPON_RULE_INVALID);
        if (BooleanUtil.isTrue(req.getAutoIssueOnRegister())) {
            AssertUtils.isTrue(ownerType == CouponOwnerTypeEnum.PLATFORM, BizErrorCode.COUPON_OWNER_INVALID);
            AssertUtils.isFalse(BooleanUtil.isTrue(req.getClaimEnabled()), BizErrorCode.COUPON_RULE_INVALID);
        }
        if (ownerType == CouponOwnerTypeEnum.SHOP) {
            AssertUtils.notNull(shopId, BizErrorCode.COUPON_OWNER_INVALID);
        }
        BigDecimal threshold = ObjectUtil.defaultIfNull(req.getThreshold(), BigDecimal.ZERO);
        switch (type) {
            case FULL_REDUCTION -> {
                AssertUtils.isTrue(
                        threshold.compareTo(BigDecimal.ZERO) > 0,
                        BizErrorCode.COUPON_RULE_INVALID
                );
                AssertUtils.isTrue(req.isDiscountAmountValid(threshold), BizErrorCode.COUPON_RULE_INVALID);
            }
            case CASH -> {
                AssertUtils.isTrue(
                        threshold.compareTo(BigDecimal.ZERO) >= 0,
                        BizErrorCode.COUPON_RULE_INVALID
                );
                AssertUtils.isTrue(req.isDiscountAmountPositive(), BizErrorCode.COUPON_RULE_INVALID);
            }
            case DISCOUNT -> {
                AssertUtils.isTrue(
                        threshold.compareTo(BigDecimal.ZERO) >= 0,
                        BizErrorCode.COUPON_RULE_INVALID
                );
                AssertUtils.isTrue(req.isDiscountRateValid(), BizErrorCode.COUPON_RULE_INVALID);
            }
        }
        if (scopeType == CouponScopeTypeEnum.PRODUCT) {
            AssertUtils.isTrue(CollUtil.isNotEmpty(req.getProductIds()), BizErrorCode.COUPON_SCOPE_INVALID);
        } else if (scopeType == CouponScopeTypeEnum.CATEGORY) {
            AssertUtils.isTrue(CollUtil.isNotEmpty(req.getCategoryIds()), BizErrorCode.COUPON_SCOPE_INVALID);
        }
        AssertUtils.isTrue(req.isIssueTimeValid(), BizErrorCode.COUPON_RULE_INVALID);
        if (validityMode == ValidityModeEnum.FIXED) {
            AssertUtils.isTrue(req.isFixedTimeValid(), BizErrorCode.COUPON_RULE_INVALID);
        } else if (validityMode == ValidityModeEnum.RELATIVE) {
            AssertUtils.isTrue(req.hasValidDays(), BizErrorCode.COUPON_RULE_INVALID);
        }
    }

    /**
     * 校验适用范围配置（商品/分类存在性，店铺券目标商品必须属于当前店铺）。
     *
     * @param scopeType    适用范围
     * @param productIds   商品 ID 列表
     * @param categoryIds  分类 ID 列表
     * @param ownerType    券归属
     * @param shopId       店铺 ID
     */
    private void validateScope(
            CouponScopeTypeEnum scopeType,
            List<Long> productIds,
            List<Long> categoryIds,
            CouponOwnerTypeEnum ownerType,
            Long shopId
    ) {
        if (scopeType == CouponScopeTypeEnum.PRODUCT) {
            List<Long> distinct = productIds.stream()
                                            .distinct()
                                            .toList();
            for (Long productId : distinct) {
                ProductResp product = callProduct(productId);
                if (ownerType == CouponOwnerTypeEnum.SHOP) {
                    AssertUtils.isTrue(shopId.equals(product.getShopId()), BizErrorCode.COUPON_SCOPE_INVALID);
                }
            }
        } else if (scopeType == CouponScopeTypeEnum.CATEGORY) {
            List<Long> distinct = categoryIds.stream()
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
        AssertUtils.isTrue(coupon.isClaimable(), BizErrorCode.COUPON_NOT_CLAIMABLE);
        if (coupon.getScopeType() == CouponScopeTypeEnum.PRODUCT) {
            Long count = couponProductMapper.selectCount(
                    Wrappers.<CouponProductDO>lambdaQuery()
                            .eq(CouponProductDO::getCouponId, coupon.getId())
            );
            AssertUtils.isTrue(count != null && count > 0, BizErrorCode.COUPON_SCOPE_INVALID);
        } else if (coupon.getScopeType() == CouponScopeTypeEnum.CATEGORY) {
            Long count = couponCategoryMapper.selectCount(
                    Wrappers.<CouponCategoryDO>lambdaQuery()
                            .eq(CouponCategoryDO::getCouponId, coupon.getId())
            );
            AssertUtils.isTrue(count != null && count > 0, BizErrorCode.COUPON_SCOPE_INVALID);
        }
    }

    /**
     * 保存范围关联（先删后插，ALL 范围清空关联）。
     *
     * @param couponId    模板 ID
     * @param scopeType   适用范围
     * @param productIds  商品 ID 列表
     * @param categoryIds 分类 ID 列表
     * @param now         当前时间
     */
    private void saveScope(
            Long couponId,
            CouponScopeTypeEnum scopeType,
            List<Long> productIds,
            List<Long> categoryIds,
            LocalDateTime now
    ) {
        if (scopeType == CouponScopeTypeEnum.PRODUCT) {
            couponProductMapper.delete(
                    Wrappers.<CouponProductDO>lambdaQuery()
                            .eq(CouponProductDO::getCouponId, couponId)
            );
            List<Long> distinct = productIds.stream()
                                            .distinct()
                                            .toList();
            for (Long productId : distinct) {
                couponProductMapper.insert(
                        CouponProductDO.builder()
                                       .couponId(couponId)
                                       .productId(productId)
                                       .createTime(now)
                                       .build()
                );
            }
        } else if (scopeType == CouponScopeTypeEnum.CATEGORY) {
            couponCategoryMapper.delete(
                    Wrappers.<CouponCategoryDO>lambdaQuery()
                            .eq(CouponCategoryDO::getCouponId, couponId)
            );
            List<Long> distinct = categoryIds.stream()
                                            .distinct()
                                            .toList();
            for (Long categoryId : distinct) {
                couponCategoryMapper.insert(
                        CouponCategoryDO.builder()
                                        .couponId(couponId)
                                        .categoryId(categoryId)
                                        .createTime(now)
                                        .build()
                );
            }
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
                Wrappers.<CouponProductDO>lambdaQuery()
                        .eq(CouponProductDO::getCouponId, couponId)
        );
        couponCategoryMapper.delete(
                Wrappers.<CouponCategoryDO>lambdaQuery()
                        .eq(CouponCategoryDO::getCouponId, couponId)
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
                Wrappers.<CouponProductDO>lambdaQuery()
                        .eq(CouponProductDO::getCouponId, couponId)
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
                Wrappers.<CouponCategoryDO>lambdaQuery()
                        .eq(CouponCategoryDO::getCouponId, couponId)
        );
        return list.stream()
                   .map(CouponCategoryDO::getCategoryId)
                   .toList();
    }

    /**
     * 追加模板操作流水。
     *
     * @param coupon  模板
     * @param from    操作前状态
     * @param to      操作后状态
     * @param action  操作类型
     * @param summary 变更摘要
     * @param reason  原因
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
                CouponOperationLogDO.builder()
                                    .couponId(coupon.getId())
                                    .operatorId(UserContextHolder.getUserId())
                                    .operatorType(resolveOperatorType())
                                    .shopId(coupon.getShopId())
                                    .fromStatus(from)
                                    .toStatus(to)
                                    .actionType(action)
                                    .changeSummary(summary)
                                    .reason(reason)
                                    .createTime(LocalDateTime.now())
                                    .build()
        );
    }

    /**
     * 从当前用户上下文解析操作人类型。
     *
     * @return 操作人类型
     */
    private CouponOperatorTypeEnum resolveOperatorType() {
        String userType = UserContextHolder.getUserType();
        if (StrUtil.isNotBlank(userType) && StrUtil.containsIgnoreCase(userType, "MERCHANT")) {
            return CouponOperatorTypeEnum.MERCHANT;
        }
        return CouponOperatorTypeEnum.OPERATOR;
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID
     */
    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        AssertUtils.notNull(userId, BizErrorCode.USER_NOT_AUTHENTICATED);
        return userId;
    }

    /**
     * 调用商品服务校验商品存在性。
     *
     * @param productId 商品 ID
     * @return 商品信息
     */
    private ProductResp callProduct(Long productId) {
        Result<ProductResp> result = productClient.getById(productId);
        AssertUtils.isTrue(
                result.getCode() == Result.SUCCESS_CODE && result.getData() != null,
                BizErrorCode.GOODS_NOT_EXIST
        );
        return result.getData();
    }

    /**
     * 调用商品服务校验分类存在性。
     *
     * @param categoryId 分类 ID
     * @return 分类信息
     */
    private CategoryResp callCategory(Long categoryId) {
        Result<CategoryResp> result = categoryClient.getById(categoryId);
        AssertUtils.isTrue(
                result.getCode() == Result.SUCCESS_CODE && result.getData() != null,
                BizErrorCode.CATEGORY_NOT_EXIST_OR_NO_ENABLE
        );
        return result.getData();
    }
}
