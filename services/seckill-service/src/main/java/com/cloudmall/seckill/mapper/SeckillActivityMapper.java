package com.cloudmall.seckill.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.seckill.entity.SeckillActivityDO;
import com.cloudmall.seckill.enums.SeckillActivityStatusEnum;
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
     * 查询临近开始（报名中/进行中）的活动，供库存预热。
     *
     * @param startBefore 开始时间阈值（早于该时间的视为临近开始）
     * @return 待预热活动列表
     */
    default List<SeckillActivityDO> listPreheatActivities(LocalDateTime startBefore) {
        return selectList(
                Wrappers.<SeckillActivityDO>lambdaQuery()
                        .in(
                                SeckillActivityDO::getStatus,
                                SeckillActivityStatusEnum.PUBLISHED,
                                SeckillActivityStatusEnum.RUNNING
                        )
                        .le(SeckillActivityDO::getStartTime, startBefore)
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
