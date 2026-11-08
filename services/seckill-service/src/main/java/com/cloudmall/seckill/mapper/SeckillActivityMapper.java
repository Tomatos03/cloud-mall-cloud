package com.cloudmall.seckill.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.seckill.enums.SeckillActivityStatusEnum;
import com.cloudmall.seckill.model.entity.SeckillActivityDO;
import com.cloudmall.seckill.model.req.SeckillActivityPageReq;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivityDO> {

    /**
     * 分页查询活动，按 ID 倒序。 <p>
     * 条件由 {@link SeckillActivityPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 活动分页数据
     */
    default IPage<SeckillActivityDO> selectActivityPage(SeckillActivityPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 判断指定整点小时是否已存在活动。
     *
     * @param hourStart 整点小时的起始时间
     * @return true 表示该整点已被占用
     */
    default boolean existsByActivityHour(LocalDateTime hourStart) {
        Long count = selectCount(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .ge(SeckillActivityDO::getStartTime, hourStart)
                        .lt(SeckillActivityDO::getStartTime, hourStart.plusHours(1))
        );
        return count != null && count > 0;
    }

    /**
     * 判断指定整点小时是否已被其他活动占用。
     *
     * @param hourStart 整点小时的起始时间
     * @param excludeId 需要排除的活动 ID
     * @return true 表示该整点已被其他活动占用
     */
    default boolean existsByActivityHourExcludingId(LocalDateTime hourStart, Long excludeId) {
        Long count = selectCount(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .ge(SeckillActivityDO::getStartTime, hourStart)
                        .lt(SeckillActivityDO::getStartTime, hourStart.plusHours(1))
                        .ne(SeckillActivityDO::getId, excludeId)
        );
        return count != null && count > 0;
    }

    /**
     * 报名中 → 进行中（到达开始时间，条件更新防并发）。
     *
     * @param now 当前时间
     * @return 影响行数
     */
    default int markStartedIfPublished(LocalDateTime now) {
        return update(
                Wrappers.<SeckillActivityDO>lambdaUpdate()
                        .set(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.RUNNING)
                        .eq(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.PUBLISHED)
                        .le(SeckillActivityDO::getStartTime, now)
        );
    }

    /**
     * 进行中 → 已结束（到达结束时间，条件更新防并发）。
     *
     * @param now 当前时间
     * @return 影响行数
     */
    default int markFinishedIfRunning(LocalDateTime now) {
        return update(
                Wrappers.<SeckillActivityDO>lambdaUpdate()
                        .set(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.FINISHED)
                        .eq(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.RUNNING)
                        .le(SeckillActivityDO::getEndTime, now)
        );
    }

    /**
     * 查询当前进行中（已开始且未结束）的活动，供库存预热。 <p>
     * 活动开始后商家禁止新增商品，此时预热不会与后续报名数据冲突； 保留 PUBLISHED 状态是为了兼
     * 容状态流转 Job 尚未执行的同一分钟。
     *
     * @param now 当前时间
     * @return 待预热活动列表
     */
    default List<SeckillActivityDO> listPreheatActivities(LocalDateTime now) {
        return selectList(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .in(
                                SeckillActivityDO::getStatus,
                                SeckillActivityStatusEnum.PUBLISHED,
                                SeckillActivityStatusEnum.RUNNING
                        )
                        .le(SeckillActivityDO::getStartTime, now)
                        .gt(SeckillActivityDO::getEndTime, now)
        );
    }

    /**
     * 查询最近结束/取消的活动，供库存清理。
     *
     * @param endAfter 结束时间阈值（晚于该时间的视为近期活动）
     * @return 待清理活动列表
     */
    default List<SeckillActivityDO> listFinishedActivities(LocalDateTime endAfter) {
        return selectList(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .in(
                                SeckillActivityDO::getStatus,
                                SeckillActivityStatusEnum.FINISHED,
                                SeckillActivityStatusEnum.CANCELLED
                        )
                        .ge(SeckillActivityDO::getEndTime, endAfter)
        );
    }

    /**
     * 查询进行中的活动（用户端，按开始时间倒序）。
     *
     * @return 进行中活动列表
     */
    default List<SeckillActivityDO> listRunningActivities() {
        return selectList(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .eq(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.RUNNING)
                        .orderByDesc(SeckillActivityDO::getStartTime)
        );
    }

    /**
     * 查询与指定时间窗有交集的可见活动（报名中/进行中/已结束），按开始时间升序。
     *
     * @param dayStart 当天零点
     * @param dayEnd 次日零点
     * @return 当天活动列表
     */
    default List<SeckillActivityDO> listTodayActivities(LocalDateTime dayStart, LocalDateTime dayEnd) {
        return selectList(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .in(
                                SeckillActivityDO::getStatus,
                                SeckillActivityStatusEnum.PUBLISHED,
                                SeckillActivityStatusEnum.RUNNING,
                                SeckillActivityStatusEnum.FINISHED
                        )
                        .lt(SeckillActivityDO::getStartTime, dayEnd)
                        .gt(SeckillActivityDO::getEndTime, dayStart)
                        .orderByAsc(SeckillActivityDO::getStartTime)
        );
    }

    /**
     * 查询当前整点场次的进行中活动（覆盖当前时刻，按开始时间倒序取最新一条）。 <p>
     * 活动固定 1 小时且整点开始，故同一时刻至多一条进行中活动。
     *
     * @param now 当前时间
     * @return 进行中活动，无则返回 null
     */
    default SeckillActivityDO selectCurrentHourActivity(LocalDateTime now) {
        return selectOne(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .eq(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.RUNNING)
                        .le(SeckillActivityDO::getStartTime, now)
                        .gt(SeckillActivityDO::getEndTime, now)
                        .orderByDesc(SeckillActivityDO::getStartTime)
                        .last(LimitEnum.ONE.getSql())
        );
    }

    /**
     * 查询报名中的活动（商家端，按开始时间升序）。
     *
     * @return 报名中活动列表
     */
    default List<SeckillActivityDO> listSignupActivities() {
        return selectList(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .eq(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.PUBLISHED)
                        .orderByAsc(SeckillActivityDO::getStartTime)
        );
    }

    /**
     * 按 ID 查询报名中的活动。
     *
     * @param id 活动 ID
     * @return 报名中活动，不存在时返回 null
     */
    default SeckillActivityDO selectPublishedById(Long id) {
        return selectOne(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .eq(SeckillActivityDO::getId, id)
                        .eq(SeckillActivityDO::getStatus, SeckillActivityStatusEnum.PUBLISHED)
        );
    }
}
