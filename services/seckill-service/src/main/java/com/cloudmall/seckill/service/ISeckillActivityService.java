package com.cloudmall.seckill.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cloudmall.seckill.api.request.ActivityCreateReq;
import com.cloudmall.seckill.api.request.ActivityUpdateReq;
import com.cloudmall.seckill.api.response.ActivityDetailResp;
import com.cloudmall.seckill.api.response.ActivityResp;
import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.req.SeckillActivityPageReq;

/**
 * 秒杀活动领域服务
 * <p>
 * 负责活动创建、发布、取消、自动流转与分页查询。
 *
 * @author : Tomatos
 */
public interface ISeckillActivityService {

    /**
     * 创建活动草稿（DRAFT）。 <p>
     * 服务端自动推导 endTime（startTime + 1小时）、校验 startTime >=
     * now+24h。
     *
     * @param createUserId 创建人用户 ID
     * @param req 创建请求（name、description、startTime 必填）
     * @return 新活动 ID
     */
    Long createDraft(Long createUserId, ActivityCreateReq req);

    /**
     * 编辑活动（仅 DRAFT 可全改；PUBLISHED 仅可改文案 name/description）
     * 。
     *
     * @param id 活动 ID
     * @param req 编辑请求
     */
    void update(Long id, ActivityUpdateReq req);

    /**
     * 发布活动：DRAFT → PUBLISHED，开放商家报名。 <p>
     * 前置：状态 DRAFT + 信息完整 + 未开始。
     *
     * @param id 活动 ID
     * @param publishUserId 发布人用户 ID
     */
    void publish(Long id, Long publishUserId);

    /**
     * 取消活动：DRAFT/PUBLISHED → CANCELLED（终态不可恢复）。 <p>
     * RUNNING/FINISHED 不可取消。
     *
     * @param id 活动 ID
     */
    void cancel(Long id);

    /**
     * 查询活动详情。
     *
     * @param id 活动 ID
     * @return 活动实体
     */
    SeckillActivityDO getById(Long id);

    /**
     * 分页查询活动列表（运营端按状态筛选）。
     *
     * @param req 查询请求
     * @return 分页结果
     */
    IPage<ActivityResp> pageActivities(SeckillActivityPageReq req);

    /**
     * 查询进行中的活动（用户端，免登录）。
     *
     * @return 进行中活动列表
     */
    List<ActivityResp> listRunningActivities();

    /**
     * 查询报名中的活动（商家端可报名列表）。
     *
     * @return 报名中活动列表
     */
    List<ActivityResp> listSignupActivities();

    /**
     * 查询活动详情（用户端，含已审核商品）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    ActivityDetailResp getActivityDetail(Long id);

    /**
     * 查询当前整点场次的进行中活动（含已审核商品）。
     *
     * @return 活动详情，当前无进行中场次时返回 null
     */
    ActivityDetailResp getCurrentHourActivity();

    /**
     * 查询报名活动详情（商家端，仅 PUBLISHED，含已审核商品）。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    ActivityDetailResp getSignupActivity(Long id);
}
