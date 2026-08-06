package com.cloudmall.seckill.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.seckill.api.response.ActivityDetailResp;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.convert.SeckillConverter;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.entity.SeckillProductDO;
import com.cloudmall.seckill.enums.SeckillActivityStateTransitions;
import com.cloudmall.seckill.enums.SeckillActivityStatusEnum;
import com.cloudmall.seckill.mapper.SeckillActivityMapper;
import com.cloudmall.seckill.mapper.SeckillProductMapper;
import com.cloudmall.seckill.model.req.SeckillActivityPageReq;
import com.cloudmall.seckill.service.ISeckillActivityService;

/**
 * 秒杀活动领域服务实现
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class SeckillActivityService implements ISeckillActivityService {

    /** 每活动最大商品数默认值 */
    private static final int DEFAULT_MAX_PRODUCTS = 50;

    /** 每活动最大商品数上限 */
    private static final int MAX_PRODUCTS_LIMIT = 500;

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper productMapper;
    private final SeckillConverter seckillConverter;

    /**
     * 创建活动草稿：校验提前量并推导结束时间。
     *
     * @param activity 活动实体
     * @return 新活动 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(SeckillActivityDO activity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = activity.getStartTime();

        // 强校验：开始时间必须晚于当前 24 小时
        Asserts.isTrue(startTime.isAfter(now.plusHours(24)), BizError.ACTIVITY_MUST_ADVANCE_24_HOURS);

        // 强校验：开始时间必须为整点（分钟、秒、纳秒均为 0）
        Asserts.isTrue(isOnTheHour(startTime), BizError.ACTIVITY_START_TIME_MUST_BE_ON_THE_HOUR);

        // 每活动最大商品数：不传默认 50，校验范围 1~500
        Integer maxProducts = activity.getMaxProducts() == null
                ? DEFAULT_MAX_PRODUCTS
                : activity.getMaxProducts();
        Asserts.isTrue(
                maxProducts >= 1 && maxProducts <= MAX_PRODUCTS_LIMIT,
                BizError.ACTIVITY_MAX_PRODUCTS_INVALID
        );
        activity.setMaxProducts(maxProducts);

        // 服务端推导结束时间：开始时间加 1 小时
        activity.setEndTime(startTime.plusHours(1));
        activity.setStatus(SeckillActivityStatusEnum.DRAFT.getCode());

        activityMapper.insert(activity);
        return activity.getId();
    }

    /**
     * 编辑活动：DRAFT 可改全字段，PUBLISHED 仅可改文案。
     *
     * @param id 活动 ID
     * @param update 编辑内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SeckillActivityDO update) {
        SeckillActivityDO existing = Asserts.requireNonNull(
                activityMapper.selectById(id),
                BizError.SECKILL_ACTIVITY_NOT_EXIST
        );
        SeckillActivityStatusEnum status = SeckillActivityStatusEnum.fromCode(existing.getStatus());

        if (status == SeckillActivityStatusEnum.DRAFT) {
            // DRAFT 状态可全改；未传的字段保持原值
            applyText(existing, update);
            if (update.getStartTime() != null) {
                LocalDateTime newStart = update.getStartTime();
                Asserts.isTrue(
                        newStart.isAfter(LocalDateTime.now().plusHours(24)),
                        BizError.ACTIVITY_MUST_ADVANCE_24_HOURS
                );
                Asserts.isTrue(isOnTheHour(newStart), BizError.ACTIVITY_START_TIME_MUST_BE_ON_THE_HOUR);
                existing.setStartTime(newStart);
                existing.setEndTime(newStart.plusHours(1));
            }
            if (update.getMaxProducts() != null) {
                Asserts.isTrue(
                        update.getMaxProducts() >= 1 && update.getMaxProducts() <= MAX_PRODUCTS_LIMIT,
                        BizError.ACTIVITY_MAX_PRODUCTS_INVALID
                );
                existing.setMaxProducts(update.getMaxProducts());
            }
        } else if (status == SeckillActivityStatusEnum.PUBLISHED) {
            // 报名期只允许改文案，时间调整需取消后重建
            applyText(existing, update);
        } else {
            throw new BizException(BizError.INVALID_ACTIVITY_STATUS);
        }

        activityMapper.updateById(existing);
    }

    /**
     * 应用文案字段，未传值时保持原值。
     *
     * @param existing 现有活动
     * @param update 编辑内容
     */
    private static void applyText(SeckillActivityDO existing, SeckillActivityDO update) {
        if (update.getName() != null) {
            existing.setName(update.getName());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
    }

    /**
     * 判断时间是否为整点（分钟、秒、纳秒均为 0）。
     *
     * @param time 时间
     * @return 是否为整点
     */
    private static boolean isOnTheHour(LocalDateTime time) {
        return time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    /**
     * 发布活动：DRAFT → PUBLISHED，开放商家报名。
     *
     * @param id 活动 ID
     * @param publishUserId 发布人用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id, Long publishUserId) {
        SeckillActivityDO activity = Asserts.requireNonNull(
                activityMapper.selectById(id),
                BizError.SECKILL_ACTIVITY_NOT_EXIST
        );
        SeckillActivityStatusEnum currentStatus = SeckillActivityStatusEnum.fromCode(activity.getStatus());

        // 前置：信息完整 + 未开始；状态合法性由迁移表判定
        Asserts.notBlank(activity.getName(), BizError.INVALID_PARAM);
        Asserts.isTrue(
                activity.getStartTime().isAfter(LocalDateTime.now()),
                BizError.SECKILL_ACTIVITY_NOT_PUBLISHABLE
        );
        Asserts.isTrue(
                SeckillActivityStateTransitions.canTransition(
                        currentStatus,
                        SeckillActivityStatusEnum.PUBLISHED
                ),
                BizError.SECKILL_ACTIVITY_NOT_PUBLISHABLE
        );

        activity.setStatus(SeckillActivityStatusEnum.PUBLISHED.getCode());
        activity.setPublishUser(publishUserId);
        activity.setPublishTime(LocalDateTime.now());

        activityMapper.updateById(activity);
    }

    /**
     * 取消活动：DRAFT/PUBLISHED → CANCELLED（终态）。
     *
     * @param id 活动 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        SeckillActivityDO activity = Asserts.requireNonNull(
                activityMapper.selectById(id),
                BizError.SECKILL_ACTIVITY_NOT_EXIST
        );
        SeckillActivityStatusEnum currentStatus = SeckillActivityStatusEnum.fromCode(activity.getStatus());

        // DRAFT/PUBLISHED 可取消，RUNNING/FINISHED 不可取消
        Asserts.isTrue(
                SeckillActivityStateTransitions.canTransition(
                        currentStatus,
                        SeckillActivityStatusEnum.CANCELLED
                ),
                BizError.SECKILL_ACTIVITY_NOT_CANCELLABLE
        );

        activity.setStatus(SeckillActivityStatusEnum.CANCELLED.getCode());
        activityMapper.updateById(activity);
    }

    /**
     * 查询活动详情。
     *
     * @param id 活动 ID
     * @return 活动实体
     */
    @Override
    public SeckillActivityDO getById(Long id) {
        return Asserts.requireNonNull(activityMapper.selectById(id), BizError.SECKILL_ACTIVITY_NOT_EXIST);
    }

    /**
     * 分页查询活动列表。
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Override
    public IPage<ActivityResp> pageActivities(SeckillActivityPageReq req) {
        IPage<SeckillActivityDO> page = activityMapper.selectActivityPage(req);

        return page.convert(seckillConverter::toActivityResp);
    }

    /**
     * 查询进行中的活动（用户端，免登录）。
     *
     * @return 进行中活动列表
     */
    @Override
    public List<ActivityResp> listRunningActivities() {
        return activityMapper.listRunningActivities()
                             .stream()
                             .map(seckillConverter::toActivityResp)
                             .collect(Collectors.toList());
    }

    /**
     * 查询报名中的活动（商家端可报名列表）。
     *
     * @return 报名中活动列表
     */
    @Override
    public List<ActivityResp> listSignupActivities() {
        return activityMapper.listSignupActivities()
                             .stream()
                             .map(seckillConverter::toActivityResp)
                             .collect(Collectors.toList());
    }

    /**
     * 查询活动详情（用户端，含已审核商品）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    @Override
    public ActivityDetailResp getActivityDetail(Long id) {
        SeckillActivityDO activity = Asserts.requireNonNull(
                activityMapper.selectById(id),
                BizError.SECKILL_ACTIVITY_NOT_EXIST
        );
        return toActivityDetail(activity);
    }

    /**
     * 查询报名活动详情（商家端，仅 PUBLISHED，含已审核商品）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    @Override
    public ActivityDetailResp getSignupActivity(Long id) {
        SeckillActivityDO activity = Asserts.requireNonNull(
                activityMapper.selectPublishedById(id),
                BizError.SECKILL_ACTIVITY_NOT_EXIST
        );
        return toActivityDetail(activity);
    }

    /**
     * 组装活动详情：活动信息 + 已审核商品列表。
     *
     * @param activity 活动实体
     * @return 活动详情响应
     */
    private ActivityDetailResp toActivityDetail(SeckillActivityDO activity) {
        List<SeckillProductDO> products = productMapper.listApprovedByActivityId(activity.getId());

        ActivityDetailResp resp = seckillConverter.toActivityDetailResp(activity);
        resp.setProducts(
                products.stream()
                        .map(seckillConverter::toSeckillProductResp)
                        .collect(Collectors.toList())
        );
        return resp;
    }
}
