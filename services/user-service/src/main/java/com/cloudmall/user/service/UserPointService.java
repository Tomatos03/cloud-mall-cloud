package com.cloudmall.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.req.IssueCouponReq;
import com.cloudmall.coupon.api.model.resp.UserCouponResp;
import com.cloudmall.user.api.model.req.PointExchangeReq;
import com.cloudmall.user.api.model.resp.PointExchangeItemResp;
import com.cloudmall.user.api.model.resp.PointExchangeResp;
import com.cloudmall.user.api.model.resp.PointTaskResp;
import com.cloudmall.user.api.model.resp.SignInResp;
import com.cloudmall.user.api.model.resp.SignInStatusResp;
import com.cloudmall.user.api.model.resp.UserPointLogResp;
import com.cloudmall.user.api.model.resp.UserPointSummaryResp;
import com.cloudmall.user.convert.PointCenterConverter;
import com.cloudmall.user.convert.UserProfileConverter;
import com.cloudmall.user.mapper.PointExchangeItemMapper;
import com.cloudmall.user.mapper.PointExchangeOrderMapper;
import com.cloudmall.user.mapper.PointTaskMapper;
import com.cloudmall.user.mapper.PointTaskRecordMapper;
import com.cloudmall.user.mapper.UserPointLogMapper;
import com.cloudmall.user.mapper.UserPointMapper;
import com.cloudmall.user.mapper.UserSignInMapper;
import com.cloudmall.user.model.entity.PointExchangeItemDO;
import com.cloudmall.user.model.entity.PointExchangeOrderDO;
import com.cloudmall.user.model.entity.PointTaskDO;
import com.cloudmall.user.model.entity.PointTaskRecordDO;
import com.cloudmall.user.model.entity.UserPointDO;
import com.cloudmall.user.model.entity.UserPointLogDO;
import com.cloudmall.user.model.entity.UserSignInDO;

/**
 * 用户积分服务：汇总、流水、签到、任务、兑换。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UserPointService {

    private static final int DAILY_SIGN_IN_BASE_POINTS = 10;
    private static final int DAILY_SIGN_IN_EXTRA_POINTS = 20;
    private static final int SIGN_IN_EXTRA_DAY = 7;
    private static final String TASK_SIGN_IN = "DAILY_SIGN_IN";
    private static final String TASK_BROWSE = "DAILY_BROWSE";
    private static final String TASK_COMMENT = "DAILY_COMMENT";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CLAIMED = "CLAIMED";
    private static final LocalDate ONCE_TASK_DATE = LocalDate.of(1970, 1, 1);

    private final UserPointMapper userPointMapper;
    private final UserPointLogMapper userPointLogMapper;
    private final UserSignInMapper userSignInMapper;
    private final PointTaskMapper pointTaskMapper;
    private final PointTaskRecordMapper pointTaskRecordMapper;
    private final PointExchangeItemMapper pointExchangeItemMapper;
    private final PointExchangeOrderMapper pointExchangeOrderMapper;
    private final UserProfileConverter userProfileConverter;
    private final PointCenterConverter pointCenterConverter;
    private final InternalCouponClient internalCouponClient;

    /**
     * 查询积分汇总，首次访问时初始化积分账户。
     *
     * @param userId 用户 ID
     * @return 积分汇总
     */
    @Transactional(rollbackFor = Exception.class)
    public UserPointSummaryResp getSummary(Long userId) {
        UserPointDO point = getOrCreate(userId);
        return userProfileConverter.toPointSummaryResp(point);
    }

    /**
     * 查询积分流水。
     *
     * @param userId 用户 ID
     * @param limit 条数上限
     * @return 积分流水
     */
    public List<UserPointLogResp> listLogs(Long userId, int limit) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        return userProfileConverter.toPointLogRespList(userPointLogMapper.listRecent(userId, limit));
    }

    /**
     * 查询今日签到状态。
     *
     * @param userId 用户 ID
     * @return 签到状态
     */
    public SignInStatusResp getSignInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        UserSignInDO signIn = userSignInMapper.selectByUserAndDate(userId, today);
        UserSignInDO yesterday = userSignInMapper.selectByUserAndDate(userId, today.minusDays(1));
        int streakDays = signIn != null
                ? signIn.getStreakDays()
                : yesterday != null ? yesterday.getStreakDays() + 1 : 1;
        int todayReward = signIn != null ? signIn.getRewardPoints() : signInReward(streakDays);
        UserPointDO point = getOrCreate(userId);
        return pointCenterConverter.toSignInStatusResp(signIn, streakDays, todayReward, point.getBalance());
    }

    /**
     * 今日签到。
     *
     * @param userId 用户 ID
     * @return 签到结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SignInResp signIn(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        LocalDate today = LocalDate.now();
        UserSignInDO existing = userSignInMapper.selectByUserAndDate(userId, today);
        UserPointDO point = getOrCreate(userId);
        if (existing != null) {
            return pointCenterConverter.toSignInResp(existing, point.getBalance());
        }
        UserSignInDO yesterday = userSignInMapper.selectByUserAndDate(userId, today.minusDays(1));
        int streakDays = yesterday != null ? yesterday.getStreakDays() + 1 : 1;
        int reward = signInReward(streakDays);
        UserSignInDO signIn = UserSignInDO.builder()
                                          .userId(userId)
                                          .signDate(today)
                                          .streakDays(streakDays)
                                          .rewardPoints(reward)
                                          .createTime(LocalDateTime.now())
                                          .build();
        userSignInMapper.insert(signIn);
        UserPointSummaryResp summary = change(userId, reward, "SIGN_IN", "每日签到");
        completeTaskRecord(userId, TASK_SIGN_IN, today, STATUS_CLAIMED);
        return pointCenterConverter.toSignInResp(signIn, summary.getBalance());
    }

    /**
     * 查询积分任务列表。
     *
     * @param userId 用户 ID
     * @return 任务列表
     */
    public List<PointTaskResp> listTasks(Long userId) {
        LocalDate today = LocalDate.now();
        return pointTaskMapper.listEnabled()
                              .stream()
                              .map(task -> toTaskResp(userId, task, today))
                              .toList();
    }

    /**
     * 完成积分任务（由业务事件触发）。
     *
     * @param userId 用户 ID
     * @param taskCode 任务编码
     */
    public void completeTask(Long userId, String taskCode) {
        PointTaskDO task = pointTaskMapper.selectByCode(taskCode);
        if (task == null || !Boolean.TRUE.equals(task.getEnabled())) {
            return;
        }
        completeTaskRecord(userId, taskCode, taskDate(task), STATUS_COMPLETED);
    }

    /**
     * 领取任务奖励。
     *
     * @param userId 用户 ID
     * @param taskCode 任务编码
     * @return 任务状态
     */
    @Transactional(rollbackFor = Exception.class)
    public PointTaskResp claimTask(Long userId, String taskCode) {
        PointTaskDO task = Asserts.requireNonNull(
                pointTaskMapper.selectByCode(taskCode),
                BizErrorEnum.DATA_NOT_FOUND
        );
        LocalDate date = taskDate(task);
        PointTaskRecordDO record = pointTaskRecordMapper.selectByUserAndCodeAndDate(userId, taskCode, date);
        Asserts.notNull(record, BizErrorEnum.POINT_TASK_NOT_COMPLETED);
        Asserts.isFalse(STATUS_CLAIMED.equals(record.getStatus()), BizErrorEnum.POINT_TASK_ALREADY_CLAIMED);
        int updated = pointTaskRecordMapper.update(
                null,
                Wrappers.<PointTaskRecordDO>lambdaUpdate()
                        .set(PointTaskRecordDO::getStatus, STATUS_CLAIMED)
                        .set(PointTaskRecordDO::getClaimTime, LocalDateTime.now())
                        .eq(PointTaskRecordDO::getId, record.getId())
                        .eq(PointTaskRecordDO::getStatus, STATUS_COMPLETED)
        );
        Asserts.isNotZero(updated, BizErrorEnum.POINT_TASK_ALREADY_CLAIMED);
        change(userId, task.getRewardPoints(), "TASK", "完成任务：" + task.getName());
        return toTaskResp(userId, task, date);
    }

    /**
     * 查询积分兑换商品。
     *
     * @param userId 用户 ID
     * @return 兑换商品列表
     */
    public List<PointExchangeItemResp> listExchangeItems(Long userId) {
        return pointExchangeItemMapper.listEnabled()
                                      .stream()
                                      .map(item -> toExchangeItemResp(userId, item))
                                      .toList();
    }

    /**
     * 转换兑换商品响应并标记当前用户兑换状态。
     *
     * @param userId 用户 ID
     * @param item 兑换商品
     * @return 兑换商品响应
     */
    private PointExchangeItemResp toExchangeItemResp(Long userId, PointExchangeItemDO item) {
        boolean exchanged = pointExchangeOrderMapper.selectSuccessByUserAndItem(userId, item.getId()) != null;
        return pointCenterConverter.toExchangeItemResp(item, exchanged);
    }

    /**
     * 积分兑换优惠券。
     *
     * @param userId 用户 ID
     * @param req 兑换请求
     * @return 兑换结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PointExchangeResp exchange(Long userId, PointExchangeReq req) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        PointExchangeItemDO item = Asserts.requireNonNull(
                pointExchangeItemMapper.selectById(req.getItemId()),
                BizErrorEnum.POINT_EXCHANGE_ITEM_NOT_FOUND
        );
        Asserts.isTrue(Boolean.TRUE.equals(item.getEnabled()), BizErrorEnum.POINT_EXCHANGE_ITEM_NOT_FOUND);
        Asserts.isTrue("COUPON".equals(item.getType()), BizErrorEnum.POINT_EXCHANGE_FAILED);
        Asserts.isNull(
                pointExchangeOrderMapper.selectSuccessByUserAndItem(userId, item.getId()),
                BizErrorEnum.POINT_EXCHANGE_ALREADY_DONE
        );

        String idempotencyKey = StrUtil.blankToDefault(req.getIdempotencyKey(), UUID.randomUUID().toString());
        PointExchangeOrderDO existing = pointExchangeOrderMapper.selectByIdempotencyKey(
                userId,
                idempotencyKey
        );
        if (existing != null && "SUCCESS".equals(existing.getStatus())) {
            UserPointDO point = getOrCreate(userId);
            return pointCenterConverter.toExchangeResp(existing, point.getBalance());
        }

        int deducted = pointExchangeItemMapper.deductStock(item.getId());
        Asserts.isNotZero(deducted, BizErrorEnum.POINT_EXCHANGE_OUT_OF_STOCK);
        UserPointSummaryResp summary = change(
                userId,
                -item.getPointsCost(),
                "EXCHANGE",
                "积分兑换：" + item.getName()
        );

        UserCouponResp coupon = internalCouponClient.issue(
                IssueCouponReq.builder()
                              .userId(userId)
                              .couponId(item.getCouponId())
                              .idempotencyKey(idempotencyKey)
                              .build()
        );
        PointExchangeOrderDO order = PointExchangeOrderDO.builder()
                                                         .userId(userId)
                                                         .itemId(item.getId())
                                                         .pointsCost(item.getPointsCost())
                                                         .status("SUCCESS")
                                                         .userCouponId(coupon == null ? null : coupon.getId())
                                                         .idempotencyKey(idempotencyKey)
                                                         .createTime(LocalDateTime.now())
                                                         .build();
        pointExchangeOrderMapper.insert(order);
        return pointCenterConverter.toExchangeResp(order, summary.getBalance());
    }

    /**
     * 变更积分并写入流水。
     *
     * @param userId 用户 ID
     * @param changeAmount 变动值
     * @param type 流水类型
     * @param description 描述
     * @return 变更后的汇总
     */
    @Transactional(rollbackFor = Exception.class)
    public UserPointSummaryResp change(Long userId, int changeAmount, String type, String description) {
        return change(userId, changeAmount, type, description, null);
    }

    /**
     * 变更积分并写入流水（可带幂等键）。
     *
     * @param userId 用户 ID
     * @param changeAmount 变动值
     * @param type 流水类型
     * @param description 描述
     * @param idempotencyKey 幂等键，可为空
     * @return 变更后的汇总
     */
    @Transactional(rollbackFor = Exception.class)
    public UserPointSummaryResp change(
        Long userId,
        int changeAmount,
        String type,
        String description,
        String idempotencyKey
    ) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        Asserts.isNotZero(changeAmount, BizErrorEnum.INVALID_PARAM);
        if (
                StrUtil.isNotBlank(idempotencyKey)
                    && userPointLogMapper.selectByIdempotencyKey(userId, idempotencyKey) != null
        ) {
            UserPointDO existing = getOrCreate(userId);
            return userProfileConverter.toPointSummaryResp(existing);
        }
        UserPointDO point = getOrCreate(userId);
        int oldBalance = point.getBalance();
        int newBalance = oldBalance + changeAmount;
        if (newBalance < 0) {
            throw new BizException(BizErrorEnum.POINT_BALANCE_NOT_ENOUGH);
        }
        int totalEarned = changeAmount > 0 ? point.getTotalEarned() + changeAmount : point.getTotalEarned();
        int totalSpent = changeAmount < 0 ? point.getTotalSpent() - changeAmount : point.getTotalSpent();
        int updated = userPointMapper.update(
                null,
                Wrappers.<UserPointDO>lambdaUpdate()
                        .set(UserPointDO::getBalance, newBalance)
                        .set(UserPointDO::getTotalEarned, totalEarned)
                        .set(UserPointDO::getTotalSpent, totalSpent)
                        .eq(UserPointDO::getUserId, userId)
                        .eq(UserPointDO::getBalance, oldBalance)
        );
        Asserts.isNotZero(updated, BizErrorEnum.SERVICE_UNAVAILABLE);
        UserPointLogDO log = UserPointLogDO.builder()
                                           .userId(userId)
                                           .changeAmount(changeAmount)
                                           .balanceAfter(newBalance)
                                           .type(type)
                                           .description(description)
                                           .idempotencyKey(idempotencyKey)
                                           .createTime(LocalDateTime.now())
                                           .build();
        userPointLogMapper.insert(log);
        return userProfileConverter.toPointSummaryResp(
                UserPointDO.builder()
                           .userId(userId)
                           .balance(newBalance)
                           .totalEarned(totalEarned)
                           .totalSpent(totalSpent)
                           .build()
        );
    }

    /**
     * 构造任务响应。
     *
     * @param userId 用户 ID
     * @param task 任务定义
     * @param date 任务日期
     * @return 任务响应
     */
    private PointTaskResp toTaskResp(Long userId, PointTaskDO task, LocalDate date) {
        PointTaskRecordDO record = pointTaskRecordMapper.selectByUserAndCodeAndDate(
                userId,
                task.getCode(),
                taskDate(task)
        );
        return pointCenterConverter.toTaskResp(
                task,
                record != null,
                record != null && STATUS_CLAIMED.equals(record.getStatus())
        );
    }

    /**
     * 写入任务完成记录（幂等）。
     *
     * @param userId 用户 ID
     * @param taskCode 任务编码
     * @param date 任务日期
     * @param status 状态
     */
    private void completeTaskRecord(Long userId, String taskCode, LocalDate date, String status) {
        PointTaskRecordDO record = pointTaskRecordMapper.selectByUserAndCodeAndDate(userId, taskCode, date);
        if (record != null) {
            return;
        }
        PointTaskDO task = pointTaskMapper.selectByCode(taskCode);
        if (task == null) {
            return;
        }
        PointTaskRecordDO created = PointTaskRecordDO.builder()
                                                     .userId(userId)
                                                     .taskCode(taskCode)
                                                     .taskDate(date)
                                                     .status(status)
                                                     .rewardPoints(task.getRewardPoints())
                                                     .createTime(LocalDateTime.now())
                                                     .build();
        pointTaskRecordMapper.insert(created);
    }

    /**
     * 计算任务日期：一次性任务使用固定日期。
     *
     * @param task 任务
     * @return 任务日期
     */
    private LocalDate taskDate(PointTaskDO task) {
        return "DAILY".equals(task.getFrequency()) ? LocalDate.now() : ONCE_TASK_DATE;
    }

    /**
     * 计算签到奖励。
     *
     * @param streakDays 连续签到天数
     * @return 奖励积分
     */
    private int signInReward(int streakDays) {
        return DAILY_SIGN_IN_BASE_POINTS
                       + (streakDays % SIGN_IN_EXTRA_DAY == 0 ? DAILY_SIGN_IN_EXTRA_POINTS : 0);
    }

    /**
     * 订单支付成功发放积分（1 元 = 1 积分，按订单号幂等）。
     *
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @param amount 实付金额
     */
    public void awardOrderPoints(Long userId, String orderNo, java.math.BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        int points = amount.setScale(0, java.math.RoundingMode.DOWN).intValue();
        if (points <= 0) {
            return;
        }
        change(userId, points, "ORDER", "购买商品 获得积分", "ORDER:" + orderNo);
    }

    /**
     * 获取积分账户，首次访问时初始化。
     *
     * @param userId 用户 ID
     * @return 积分账户
     */
    private UserPointDO getOrCreate(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        UserPointDO point = userPointMapper.selectById(userId);
        if (point != null) {
            return point;
        }
        point = UserPointDO.builder()
                           .userId(userId)
                           .balance(0)
                           .totalEarned(0)
                           .totalSpent(0)
                           .build();
        userPointMapper.insert(point);
        return point;
    }
}
